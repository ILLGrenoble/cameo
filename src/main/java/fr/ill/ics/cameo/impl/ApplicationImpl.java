/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.impl;

import org.zeromq.ZMsg;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.Application.Handler;
import fr.ill.ics.cameo.Application.State;
import fr.ill.ics.cameo.CancelEvent;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.Event;
import fr.ill.ics.cameo.EventListener;
import fr.ill.ics.cameo.InvalidArgumentException;
import fr.ill.ics.cameo.PublisherCreationException;
import fr.ill.ics.cameo.PublisherDestructionException;
import fr.ill.ics.cameo.RequesterCreationException;
import fr.ill.ics.cameo.ResponderCreationException;
import fr.ill.ics.cameo.StatusEvent;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.UnmanagedApplicationException;
import fr.ill.ics.cameo.WaitingSet;
import fr.ill.ics.cameo.proto.Messages;
import fr.ill.ics.cameo.proto.Messages.ConnectPortCommand;
import fr.ill.ics.cameo.proto.Messages.CreatePublisherCommand;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;
import fr.ill.ics.cameo.proto.Messages.PublisherResponse;
import fr.ill.ics.cameo.proto.Messages.RemovePortCommand;
import fr.ill.ics.cameo.proto.Messages.RequestPortCommand;
import fr.ill.ics.cameo.proto.Messages.RequestResponse;
import fr.ill.ics.cameo.proto.Messages.SetResultCommand;
import fr.ill.ics.cameo.proto.Messages.SetStatusCommand;
import fr.ill.ics.cameo.proto.Messages.StopCommand;
import fr.ill.ics.cameo.proto.Messages.TerminatePublisherCommand;

public class ApplicationImpl extends ServicesImpl {
	
	private String name;
	private int id;
	private boolean managed = false;

	private String starterEndpoint;
	private String starterName;
	private int starterId;
	
	// Definition of a EventListener member.
	private EventListener eventListener = new EventListener();
	private HandlerImpl stopHandler;
	private WaitingSet waitingSet = new WaitingSet();
	
	/**
	 * Constructor with application arguments.
	 * This constructor must be used when the services are related to the cameo server that
	 * has started the current application.
	 * Some methods may throw the runtime ConnectionTimeout exception, so it is recommended to catch the exception at a global scope if a timeout is set.
	 * @param args
	 */
	public ApplicationImpl(String[] args) {
		
		id = -1;
		
		// analysing args to get the endpoint and the id
		if (args.length == 0) {
			throw new InvalidArgumentException("missing info argument");			
		}
		
		// the last argument contains the necessary information
		String info = args[args.length - 1];
		String[] tokens = info.split(":");

		// check length
		if (tokens.length < 4) {
			throw new InvalidArgumentException(info + " is not a valid argument");
		}

		// Init the services part.
		init();
		
		url = tokens[0] + ":" + tokens[1];
		port = Integer.parseInt(tokens[2]);
		
		// We separated host endpoint and server in the past (server being tcp://localhost)
		// but that generates troubles when two applications communicate remotely.
		// However leave the same value seems to be ok.
		serverEndpoint = url + ":" + port;
		
		// Analyze 4th token that can be either the name.id or the name in case of unmanaged application. 
		String nameId = tokens[3];
		int index = nameId.lastIndexOf('.');
		
		if (index != -1) {
			managed = true;
			name = nameId.substring(0, index);
			id = Integer.parseInt(nameId.substring(index + 1));
		}
		else {
			managed = false;
			name = nameId;
			id = initUnmanagedApplication();
			
			if (id == -1) {
				throw new UnmanagedApplicationException("Maximum number of applications " + name + " reached");
			}
		}
		
		// Search for the starter reference.
		if (tokens.length >= 7) {
			index = tokens[4].lastIndexOf('@');
			starterEndpoint = tokens[4].substring(index + 1) + ":" + tokens[5] + ":" + tokens[6]; 
			String starterNameId = tokens[4].substring(0, index);
			index = starterNameId.lastIndexOf('.');
			starterName = starterNameId.substring(0, index);
			starterId = Integer.parseInt(starterNameId.substring(index + 1));
		}
		
		// Init listener
		eventListener.setName(name);
	}
	
	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
	
	public String getEndpoint() {
		return serverEndpoint;
	}
	
	public String getStarterEndpoint() {
		return starterEndpoint;
	}
	
	public String getStarterName() {
		return starterName;
	}
	
	public int getStarterId() {
		return starterId;
	}
		
	public WaitingSet getWaitingSet() {
		return waitingSet;
	}
	
	public void terminate() {
		
		if (stopHandler != null) {
			stopHandler.terminate();
		}
		
		waitingSet.terminateAll();

		// Tell the cameo server that the application is terminated if it is unmanaged.
		if (!managed) {
			terminateUnmanagedApplication();
		}
		
		super.terminate();
	}
	
	public void setResult(byte[] data) {
		
		ZMsg request = createSetResultRequest(id, data);
		
		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = RequestResponse.parseFrom(messageData);
			
			if (requestResponse.getValue() == -1) {
				throw new UnexpectedException("Cannot set result");
			}
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public void setResult(String data) {
		setResult(Buffer.serialize(data));		
	}
	
	public void setResult(int[] data) {
		setResult(Buffer.serialize(data));
	}

	public void setResult(long[] data) {
		setResult(Buffer.serialize(data));
	}
	
	public void setResult(float[] data) {
		setResult(Buffer.serialize(data));
	}
	
	public void setResult(double[] data) {
		setResult(Buffer.serialize(data));
	}
		
	/**
	 * Sets the owner application RUNNING.
	 * @return
	 * @throws StateException, ConnectionTimeout
	 */
	public boolean setRunning() {
		
		ZMsg request = createSetStatusRequest(id, Application.State.RUNNING);
		
		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = RequestResponse.parseFrom(messageData);
			
			if (requestResponse.getValue() == -1) {
				return false;
			}
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		return true;
	}

	private int initUnmanagedApplication() {
		
		ZMsg request = createStartedUnmanagedRequest(name);
		
		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = RequestResponse.parseFrom(messageData);
			
			return requestResponse.getValue();
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}		
	}
	
	private void terminateUnmanagedApplication() {
		
		ZMsg request = createTerminatedUnmanagedRequest(id);
		
		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = RequestResponse.parseFrom(messageData);
						
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}		
	}
		
	/**
	 * Gets the application state.
	 * @return
	 * @throws ConnectionTimeout
	 */
	private int getState(int id) {
		
		ZMsg request = createGetStatusRequest(id);
		
		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			
			if (messageData == null) {
				return Application.State.UNKNOWN;
			}
			
			Messages.StatusEvent requestResponse = Messages.StatusEvent.parseFrom(messageData);
			
			return requestResponse.getApplicationState();
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	
	/**
	 * Returns true if the application is in STOPPING state. Can be used when the application is already polling.
	 * @return
	 */
	public boolean isStopping() {
		return (getState(id) == Application.State.STOPPING);
	}
		
	/**
	 * Waits for the stop event.
	 */
	public int waitForStop() {
				
		// Test actual state
		int state = getState(id);
		if (state == Application.State.STOPPING 
			|| state == Application.State.KILLING) {
			return state;
		}
		
		while (true) {
			// waits for a new incoming status
			Event event = eventListener.popEvent();
			
			if (event.getId() == id) {
			
				if (event instanceof StatusEvent) {
				
					StatusEvent status = (StatusEvent)event;
					state = status.getState();
										
					if (state == Application.State.STOPPING) {
						return state;
					}
					
				} else if (event instanceof CancelEvent) {
					return State.UNKNOWN;
				}
			}
		}
	}
	
	
	public void terminateWaitForStop() {
		eventListener.cancel(id);		
	}
	
	/**
	 * 
	 */
	public void createStopHandler(Handler handler) {
		stopHandler = new HandlerImpl(this, handler);
		stopHandler.start();
	}
	

	public EventListener getEventListener() {
		return eventListener;
	}

	public void cancelWaitings() {
		waitingSet.cancelAll();		
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws PublisherCreationException, ConnectionTimeout
	 */
	public PublisherImpl publish(String name, int numberOfSubscribers) throws PublisherCreationException {
		
		ZMsg request = createCreatePublisherRequest(id, name, numberOfSubscribers);

		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			PublisherResponse requestResponse = null;
				
			requestResponse = PublisherResponse.parseFrom(messageData);
			
			int publisherPort = requestResponse.getPublisherPort();
			if (publisherPort == -1) {
				throw new PublisherCreationException(requestResponse.getMessage());
			}
			int synchronizerPort = requestResponse.getSynchronizerPort();
			
			return new PublisherImpl(this, context, publisherPort, synchronizerPort, name, numberOfSubscribers);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws PublisherCreationException, ConnectionTimeout
	 */
	public PublisherImpl publish(String name) throws PublisherCreationException {
		return publish(name, 0);
	}
	
	/**
	 * 
	 * @param name
	 * @throws PublisherDestructionException, ConnectionTimeout
	 */
	void destroyPublisher(String name) throws PublisherDestructionException {
		
		ZMsg request = createTerminatePublisherRequest(id, name);
		
		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = null;

			requestResponse = RequestResponse.parseFrom(messageData);
			
			int value = requestResponse.getValue();
			if (value == -1) {
				throw new PublisherDestructionException(requestResponse.getMessage());
			}
			
		
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
		
	/**
	 * 
	 * @param name
	 * @return
	 * @throws ResponderCreationException, ConnectionTimeout
	 */
	public ResponderImpl respond(String name) throws ResponderCreationException {
		
		String portName = ResponderImpl.RESPONDER_PREFIX + name;
		ZMsg request = createRequestPortRequest(id, portName);

		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = null;
				
			requestResponse = RequestResponse.parseFrom(messageData);
			
			int port = requestResponse.getValue();
			if (port == -1) {
				throw new ResponderCreationException(requestResponse.getMessage());
			}
			
			return new ResponderImpl(this, context, port, name);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	public RequesterImpl request(String name, InstanceImpl instanceImpl) throws RequesterCreationException {
		
		int responderId = instanceImpl.getId();
		String responderUrl = instanceImpl.getUrl();
		String responderEndpoint = instanceImpl.getEndpoint();
		
		String responderPortName = ResponderImpl.RESPONDER_PREFIX + name;
		String requesterPortName = RequesterImpl.getRequesterPortName(name, responderId);
		
		try {
			// First connect to the responder
			ZMsg request = createConnectPortRequest(responderId, responderPortName);
					
			ZMsg reply = tryRequest(request, responderEndpoint);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = RequestResponse.parseFrom(messageData);
			
			int responderPort = requestResponse.getValue();
			if (responderPort == -1) {
				
				// Wait for the responder port.
				instanceImpl.waitFor(0, responderPortName);

				// Retry to connect
				request = createConnectPortRequest(responderId, responderPortName);
				
				reply = tryRequest(request, responderEndpoint);
				messageData = reply.getFirst().getData();
				requestResponse = RequestResponse.parseFrom(messageData);
								
				responderPort = requestResponse.getValue();
				if (responderPort == -1) {
					throw new RequesterCreationException(requestResponse.getMessage());
				}
			}
			
			// Request a requester port
			request = createRequestPortRequest(id, requesterPortName);
			
			reply = tryRequest(request);
			messageData = reply.getFirst().getData();
			requestResponse = RequestResponse.parseFrom(messageData);
			
			int requesterPort = requestResponse.getValue();
			if (requesterPort == -1) {
				throw new RequesterCreationException(requestResponse.getMessage());
			}
			
			return new RequesterImpl(this, context, responderUrl, requesterPort, responderPort, name, responderId);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public void removePort(String name) {
		
		ZMsg request = createRemovePortRequest(id, name);

		try {
			ZMsg reply = tryRequest(request);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = null;
				
			requestResponse = RequestResponse.parseFrom(messageData);
			
			int port = requestResponse.getValue();
			if (port == -1) {
				System.err.println("Cannot remove port " + name);
			}
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	private ZMsg createSetStatusRequest(int id, int state) {
		
		ZMsg request = createRequest(Type.SETSTATUS);
		SetStatusCommand command = SetStatusCommand.newBuilder().setId(id).setApplicationState(state).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	private ZMsg createRequestPortRequest(int id, String name) {
		
		ZMsg request = createRequest(Type.REQUESTPORT);
		RequestPortCommand command = RequestPortCommand.newBuilder()
																.setId(id)
																.setName(name)
																.build();
		request.add(command.toByteArray());
		
		return request;
	}

	private ZMsg createConnectPortRequest(int id, String name) {
		
		ZMsg request = createRequest(Type.CONNECTPORT);
		ConnectPortCommand command = ConnectPortCommand.newBuilder()
																.setId(id)
																.setName(name)
																.build();
		request.add(command.toByteArray());
		
		return request;
	}

	private ZMsg createRemovePortRequest(int id, String name) {
		
		ZMsg request = createRequest(Type.REMOVEPORT);
		RemovePortCommand command = RemovePortCommand.newBuilder()
																.setId(id)
																.setName(name)
																.build();
		request.add(command.toByteArray());
		
		return request;
	}

	
	
	private ZMsg createCreatePublisherRequest(int id, String name, int numberOfSubscribers) {
		
		ZMsg request = createRequest(Type.CREATEPUBLISHER);
		CreatePublisherCommand command = CreatePublisherCommand.newBuilder()
																.setId(id)
																.setName(name)
																.setNumberOfSubscribers(numberOfSubscribers)
																.build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	private ZMsg createSetResultRequest(int id, byte[] result) {

		ByteString data = ByteString.copyFrom(result);
		
		ZMsg request = createRequest(Type.SETRESULT);
		SetResultCommand command = SetResultCommand.newBuilder().setId(id).setData(data).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	private ZMsg createTerminatePublisherRequest(int id, String name) {
		
		ZMsg request = createRequest(Type.TERMINATEPUBLISHER);
		TerminatePublisherCommand command = TerminatePublisherCommand.newBuilder().setId(id).setName(name).build();
		request.add(command.toByteArray());
		
		return request;
	}
		
	private ZMsg createStopRequest(int id) {
		
		ZMsg request = createRequest(Type.STOP);
		StopCommand command = StopCommand.newBuilder().setId(id).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	@Override
	public String toString() {
		return name + "." + id + "@" + serverEndpoint;
	}

}