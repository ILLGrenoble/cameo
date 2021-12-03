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

package fr.ill.ics.cameo.base.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.ProcessHandlerImpl;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Msg;
import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.CancelEvent;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.Event;
import fr.ill.ics.cameo.base.EventListener;
import fr.ill.ics.cameo.base.InvalidArgumentException;
import fr.ill.ics.cameo.base.StatusEvent;
import fr.ill.ics.cameo.base.UnexpectedException;
import fr.ill.ics.cameo.base.UnmanagedApplicationException;
import fr.ill.ics.cameo.base.WaitingSet;
import fr.ill.ics.cameo.base.Application.Handler;
import fr.ill.ics.cameo.base.Application.State;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
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
		serverEndpoint = Endpoint.parse(JSON.getString(infoObject, Messages.ApplicationIdentity.SERVER));
		
		// Init the context and socket.
		init();
		
		// Retrieve the server version.
		retrieveServerVersion();

		// Get the name present for both managed and unmanaged apps.
		name = JSON.getString(infoObject, Messages.ApplicationIdentity.NAME);

		// For managed apps, id is present in info.
		if (infoObject.containsKey(Messages.ApplicationIdentity.ID)) {
			managed = true;
			id = JSON.getInt(infoObject, Messages.ApplicationIdentity.ID);
		}
		else {
			managed = false;
			id = initUnmanagedApplication();
			
			if (id == -1) {
				throw new UnmanagedApplicationException("Maximum number of applications " + name + " reached");
			}
		}
		
		// Get the starter info if it is present.
		if (infoObject.containsKey(Messages.ApplicationIdentity.STARTER)) {
			JSONObject starterObject = JSON.getObject(infoObject, Messages.ApplicationIdentity.STARTER);
			starterEndpoint = Endpoint.parse(JSON.getString(starterObject, Messages.ApplicationIdentity.SERVER));
			starterName = JSON.getString(starterObject, Messages.ApplicationIdentity.NAME);
			starterId = JSON.getInt(starterObject, Messages.ApplicationIdentity.ID);
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
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(Messages.createSetResultRequest(id)));
		
		// Add the data in a second frame.
		message.add(data);
		
		try {
			Zmq.Msg reply = requestSocket.request(message);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
			if (value == -1) {
				throw new UnexpectedException("Cannot set result");
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public void setResult(String data) {
		setResult(Messages.serialize(data));		
	}
		
	/**
	 * Sets the owner application RUNNING.
	 * @return
	 * @throws StateException, ConnectionTimeout
	 */
	public boolean setRunning() {
		
		JSONObject request = Messages.createSetStatusRequest(id, Application.State.RUNNING);
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
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
		
		try {
			Zmq.Msg reply = requestSocket.request(Messages.createAttachUnmanagedRequest(name, pid));
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return JSON.getInt(response, Messages.RequestResponse.VALUE);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}		
	}
	
	private void terminateUnmanagedApplication() {
		requestSocket.request(Messages.createDetachUnmanagedRequest(id));
	}
	
	private void setStopHandler(int stoppingTime) {
		requestSocket.request(Messages.createSetStopHandlerRequest(id, stoppingTime));
	}
	
	/**
	 * Gets the application state.
	 * @return
	 * @throws ConnectionTimeout
	 */
	private int getState(int id) {
		
		try {
			Zmq.Msg reply = requestSocket.request(Messages.createGetStatusRequest(id));
			
			byte[] messageData = reply.getFirstData();
			
			if (messageData == null) {
				return Application.State.UNKNOWN;
			}
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			return JSON.getInt(response, Messages.StatusEvent.APPLICATION_STATE);
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
	
	public void removePort(String name) {
		
		JSONObject request = Messages.createRemovePortV0Request(id, name);

		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			int port = JSON.getInt(response, Messages.RequestResponse.VALUE);
			if (port == -1) {
				System.err.println("Cannot remove port " + name);
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	public JSONObject request(JSONObject request) {
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			
			// Get the JSON response object.
			return parse(reply);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	@Override
	public String toString() {
		return name + "." + id + "@" + serverEndpoint;
	}

}