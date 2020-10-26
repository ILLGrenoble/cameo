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

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.Application.Handler;
import fr.ill.ics.cameo.Application.State;
import fr.ill.ics.cameo.CancelEvent;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.Event;
import fr.ill.ics.cameo.EventListener;
import fr.ill.ics.cameo.InvalidArgumentException;
import fr.ill.ics.cameo.ProcessHandlerImpl;
import fr.ill.ics.cameo.PublisherCreationException;
import fr.ill.ics.cameo.PublisherDestructionException;
import fr.ill.ics.cameo.RequesterCreationException;
import fr.ill.ics.cameo.ResponderCreationException;
import fr.ill.ics.cameo.StatusEvent;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.UnmanagedApplicationException;
import fr.ill.ics.cameo.WaitingSet;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.strings.Endpoint;

public class ThisImpl extends ServicesImpl {
	
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
	public ThisImpl(String[] args) {
		
		id = -1;
		
		// analysing args to get the endpoint and the id
		if (args.length == 0) {
			throw new InvalidArgumentException("missing info argument");			
		}
		
		// the last argument contains the necessary information
		String info = args[args.length - 1];
		
		System.out.println("info = " + info);
		
		// Parse the info.
		JSONObject infoObject;
		try {
			infoObject = parser.parse(info);
		}
		catch (ParseException e) {
			throw new InvalidArgumentException("bad format for info argument");
		}
		
//		String[] tokens = info.split(":");
//
//		// check length
//		if (tokens.length < 4) {
//			throw new InvalidArgumentException(info + " is not a valid argument");
//		}
//
//		url = tokens[0] + ":" + tokens[1];
//		port = Integer.parseInt(tokens[2]);

		serverEndpoint = Endpoint.parse(JSON.getString(infoObject, Message.ApplicationIdentity.SERVER));
		url = serverEndpoint.getProtocol() + "://" + serverEndpoint.getAddress();
		
		// Init the context and socket.
		init();
		
		// Retrieve the server version.
		retrieveServerVersion();
		
//		// Analyze 4th token that can be either the name.id or the name in case of unmanaged application. 
//		String nameId = tokens[3];
//		int index = nameId.lastIndexOf('.');
		
		name = JSON.getString(infoObject, Message.ApplicationIdentity.NAME);
		
//		if (index != -1) {
		if (infoObject.containsKey(Message.ApplicationIdentity.ID)) {
			managed = true;
//			name = nameId.substring(0, index);
//			id = Integer.parseInt(nameId.substring(index + 1));
			
			
			id = JSON.getInt(infoObject, Message.ApplicationIdentity.ID);
		}
		else {
			managed = false;
			//name = nameId;
			
			id = initUnmanagedApplication();
			
			if (id == -1) {
				throw new UnmanagedApplicationException("Maximum number of applications " + name + " reached");
			}
		}
		
//		// Search for the starter reference.
//		if (tokens.length >= 7) {
//			index = tokens[4].lastIndexOf('@');
//			starterEndpoint = tokens[4].substring(index + 1) + ":" + tokens[5] + ":" + tokens[6]; 
//			String starterNameId = tokens[4].substring(0, index);
//			index = starterNameId.lastIndexOf('.');
//			starterName = starterNameId.substring(0, index);
//			starterId = Integer.parseInt(starterNameId.substring(index + 1));
//		}
		
		System.out.println("endpoint = " + serverEndpoint);
		System.out.println("name = " + name);
		System.out.println("id = " + id);
		System.out.println("starterEndpoint = " + starterEndpoint);
		System.out.println("starterName = " + starterName);
		System.out.println("starterId = " + starterId);
				
		// Init listener.
		eventListener.setName(name);
	}
	
	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}
	
	public Endpoint getEndpoint() {
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
		
		Zmq.Msg request = createSetResultRequest(id, data);
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			int value = JSON.getInt(response, Message.RequestResponse.VALUE);
			if (value == -1) {
				throw new UnexpectedException("Cannot set result");
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public void setResult(String data) {
		setResult(Message.serialize(data));		
	}
		
	/**
	 * Sets the owner application RUNNING.
	 * @return
	 * @throws StateException, ConnectionTimeout
	 */
	public boolean setRunning() {
		
		Zmq.Msg request = createSetStatusRequest(id, Application.State.RUNNING);
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			int value = JSON.getInt(response, Message.RequestResponse.VALUE);
			if (value == -1) {
				return false;
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		return true;
	}

	private int initUnmanagedApplication() {
		
		// Get the pid.
		long pid = ProcessHandlerImpl.pid();
		
		Zmq.Msg request = createAttachUnmanagedRequest(name, pid);
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return JSON.getInt(response, Message.RequestResponse.VALUE);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}		
	}
	
	private void terminateUnmanagedApplication() {
		
		Zmq.Msg request = createDetachUnmanagedRequest(id);
		
		requestSocket.request(request);
	}
		
	/**
	 * Gets the application state.
	 * @return
	 * @throws ConnectionTimeout
	 */
	private int getState(int id) {
		
		Zmq.Msg request = createGetStatusRequest(id);
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			byte[] messageData = reply.getFirstData();
			
			if (messageData == null) {
				return Application.State.UNKNOWN;
			}
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			return JSON.getInt(response, Message.StatusEvent.APPLICATION_STATE);
		}
		catch (ParseException e) {
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
		
		Zmq.Msg request = createCreatePublisherRequest(id, name, numberOfSubscribers);
		
		try {
			Zmq.Msg reply = requestSocket.request(request);

			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			int publisherPort = JSON.getInt(response, Message.PublisherResponse.PUBLISHER_PORT);
			if (publisherPort == -1) {
				throw new PublisherCreationException(JSON.getString(response, Message.PublisherResponse.MESSAGE));
			}
			int synchronizerPort = JSON.getInt(response, Message.PublisherResponse.SYNCHRONIZER_PORT);
			
			return new PublisherImpl(this, context, publisherPort, synchronizerPort, name, numberOfSubscribers);
		}
		catch (ParseException e) {
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
		
		Zmq.Msg request = createTerminatePublisherRequest(id, name);
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			int value = JSON.getInt(response, Message.RequestResponse.VALUE);
			if (value == -1) {
				throw new PublisherDestructionException(JSON.getString(response, Message.RequestResponse.MESSAGE));
			}
		}
		catch (ParseException e) {
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
		Zmq.Msg request = createRequestPortV0Request(id, portName);

		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			int port = JSON.getInt(response, Message.RequestResponse.VALUE);
			if (port == -1) {
				throw new ResponderCreationException(JSON.getString(response, Message.RequestResponse.MESSAGE));
			}
			
			return new ResponderImpl(this, context, port, name);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	public RequesterImpl request(String name, InstanceImpl instanceImpl) throws RequesterCreationException {
		
		int responderId = instanceImpl.getId();
		String responderUrl = instanceImpl.getUrl();
		String responderEndpoint = instanceImpl.getEndpoint().toString();
		
		String responderPortName = ResponderImpl.RESPONDER_PREFIX + name;
		
		int requesterId = RequesterImpl.newRequesterId();
		String requesterPortName = RequesterImpl.getRequesterPortName(name, responderId, requesterId);

		// Create the responder socket that will be called twice.
		RequestSocket responderSocket = createRequestSocket(responderEndpoint);
		
		try {
			// First connect to the responder.
			Zmq.Msg request = createConnectPortV0Request(responderId, responderPortName);
			Zmq.Msg reply = responderSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			int responderPort = JSON.getInt(response, Message.RequestResponse.VALUE);
			if (responderPort == -1) {
				
				// Wait for the responder port.
				instanceImpl.waitFor(responderPortName);

				// Retry to connect.
				request = createConnectPortV0Request(responderId, responderPortName);
				reply = responderSocket.request(request);
				response = parse(reply);
				responderPort = JSON.getInt(response, Message.RequestResponse.VALUE);
				
				if (responderPort == -1) {
					throw new RequesterCreationException(JSON.getString(response, Message.RequestResponse.MESSAGE));
				}
			}
			
			// Request a requester port.
			request = createRequestPortV0Request(id, requesterPortName);
			
			reply = requestSocket.request(request);
			response = parse(reply);
			int requesterPort = JSON.getInt(response, Message.RequestResponse.VALUE);
			
			if (requesterPort == -1) {
				throw new RequesterCreationException(JSON.getString(response, Message.RequestResponse.MESSAGE));
			}
			
			return new RequesterImpl(this, context, responderUrl, requesterPort, responderPort, name, responderId, requesterId);
			
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		finally {
			responderSocket.terminate();
		}
	}
	
	public void removePort(String name) {
		
		Zmq.Msg request = createRemovePortV0Request(id, name);

		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			int port = JSON.getInt(response, Message.RequestResponse.VALUE);
			if (port == -1) {
				System.err.println("Cannot remove port " + name);
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	private Zmq.Msg createSetStatusRequest(int id, int state) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.SET_STATUS);
		request.put(Message.SetStatusRequest.ID, id);
		request.put(Message.SetStatusRequest.APPLICATION_STATE, state);

		return message(request);
	}
	
	private Zmq.Msg createRequestPortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.REQUEST_PORT_v0);
		request.put(Message.RequestPortV0Request.ID, id);
		request.put(Message.RequestPortV0Request.NAME, name);

		return message(request);
	}

	private Zmq.Msg createConnectPortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.CONNECT_PORT_v0);
		request.put(Message.ConnectPortV0Request.ID, id);
		request.put(Message.ConnectPortV0Request.NAME, name);

		return message(request);
	}

	private Zmq.Msg createRemovePortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.REMOVE_PORT_v0);
		request.put(Message.RemovePortV0Request.ID, id);
		request.put(Message.RemovePortV0Request.NAME, name);

		return message(request);
	}
	
	private Zmq.Msg createCreatePublisherRequest(int id, String name, int numberOfSubscribers) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.CREATE_PUBLISHER_v0);
		request.put(Message.CreatePublisherRequest.ID, id);
		request.put(Message.CreatePublisherRequest.NAME, name);
		request.put(Message.CreatePublisherRequest.NUMBER_OF_SUBSCRIBERS, numberOfSubscribers);

		return message(request);
	}
	
	private Zmq.Msg createSetResultRequest(int id, byte[] result) {

		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.SET_RESULT);
		request.put(Message.SetResultRequest.ID, id);

		Zmq.Msg messageResult = message(request);

		// Add the result in a second frame.
		messageResult.add(result);
		
		return messageResult;
	}
	
	private Zmq.Msg createTerminatePublisherRequest(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.TERMINATE_PUBLISHER_v0);
		request.put(Message.TerminatePublisherRequest.ID, id);
		request.put(Message.TerminatePublisherRequest.NAME, name);

		return message(request);
	}
		
	@Override
	public String toString() {
		return name + "." + id + "@" + serverEndpoint;
	}

}