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
import fr.ill.ics.cameo.Zmq.Msg;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.strings.Endpoint;

public class ThisImpl extends ServicesImpl {
	
	private String name;
	private int id = -1;
	private boolean managed = false;

	private Endpoint starterEndpoint;
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
		
		// Analyse the args to get the info.
		if (args.length == 0) {
			throw new InvalidArgumentException("Missing info argument");			
		}
		
		// The last argument contains the necessary information.
		String info = args[args.length - 1];
		
		// Parse the info.
		JSONObject infoObject;
		try {
			infoObject = parser.parse(info);
		}
		catch (ParseException e) {
			throw new InvalidArgumentException("Bad format for info argument");
		}

		// Get the server endpoint.
		serverEndpoint = Endpoint.parse(JSON.getString(infoObject, Message.ApplicationIdentity.SERVER));
		
		// Init the context and socket.
		init();
		
		// Retrieve the server version.
		retrieveServerVersion();

		// Get the name present for both managed and unmanaged apps.
		name = JSON.getString(infoObject, Message.ApplicationIdentity.NAME);

		// For managed apps, id is present in info.
		if (infoObject.containsKey(Message.ApplicationIdentity.ID)) {
			managed = true;
			id = JSON.getInt(infoObject, Message.ApplicationIdentity.ID);
		}
		else {
			managed = false;
			id = initUnmanagedApplication();
			
			if (id == -1) {
				throw new UnmanagedApplicationException("Maximum number of applications " + name + " reached");
			}
		}
		
		// Get the starter info if it is present.
		if (infoObject.containsKey(Message.ApplicationIdentity.STARTER)) {
			JSONObject starterObject = JSON.getObject(infoObject, Message.ApplicationIdentity.STARTER);
			starterEndpoint = Endpoint.parse(JSON.getString(starterObject, Message.ApplicationIdentity.SERVER));
			starterName = JSON.getString(starterObject, Message.ApplicationIdentity.NAME);
			starterId = JSON.getInt(starterObject, Message.ApplicationIdentity.ID);
		}
				
		// Init listener.
		eventListener.setName(name);
	}
	
	public ThisImpl(String name, String endpoint) {
		
		// Get the server endpoint.
		serverEndpoint = Endpoint.parse(endpoint);
		
		// Init the context and socket.
		init();
		
		// Retrieve the server version.
		retrieveServerVersion();

		// Get the name.
		this.name = name; 
		
		// This is de-facto an unmanaged application.		
		managed = false;
		id = initUnmanagedApplication();
		
		if (id == -1) {
			throw new UnmanagedApplicationException("Maximum number of applications " + name + " reached");
		}
						
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
		
	public Endpoint getStarterEndpoint() {
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
	
	private void setStopHandler(int stoppingTime) {
		
		Zmq.Msg request = createSetStopHandlerRequest(id, stoppingTime);
		
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
				
		// Warning, this method is executed in a parallel thread.
		int state = Application.State.UNKNOWN; 
		
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
	public void createStopHandler(Handler handler, int stoppingTime) {
		
		if (handler == null) {
			return;
		}
		
		// Notify the server.
		setStopHandler(stoppingTime);
		
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

	public JSONObject request(Msg request) {
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			return parse(reply);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	private static Zmq.Msg createSetStatusRequest(int id, int state) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.SET_STATUS);
		request.put(Message.SetStatusRequest.ID, id);
		request.put(Message.SetStatusRequest.APPLICATION_STATE, state);

		return message(request);
	}
	
	public static Zmq.Msg createRequestPortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.REQUEST_PORT_v0);
		request.put(Message.RequestPortV0Request.ID, id);
		request.put(Message.RequestPortV0Request.NAME, name);

		return message(request);
	}

	public static Zmq.Msg createConnectPortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.CONNECT_PORT_v0);
		request.put(Message.ConnectPortV0Request.ID, id);
		request.put(Message.ConnectPortV0Request.NAME, name);

		return message(request);
	}

	public static Zmq.Msg createRemovePortV0Request(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.REMOVE_PORT_v0);
		request.put(Message.RemovePortV0Request.ID, id);
		request.put(Message.RemovePortV0Request.NAME, name);

		return message(request);
	}
	
	public static Zmq.Msg createCreatePublisherRequest(int id, String name, int numberOfSubscribers) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.CREATE_PUBLISHER_v0);
		request.put(Message.CreatePublisherRequest.ID, id);
		request.put(Message.CreatePublisherRequest.NAME, name);
		request.put(Message.CreatePublisherRequest.NUMBER_OF_SUBSCRIBERS, numberOfSubscribers);

		return message(request);
	}
	
	private static Zmq.Msg createSetResultRequest(int id, byte[] result) {

		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.SET_RESULT);
		request.put(Message.SetResultRequest.ID, id);

		Zmq.Msg messageResult = message(request);

		// Add the result in a second frame.
		messageResult.add(result);
		
		return messageResult;
	}
	
	public static Zmq.Msg createTerminatePublisherRequest(int id, String name) {
		
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