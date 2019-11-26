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

import java.util.List;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.Application.Info;
import fr.ill.ics.cameo.Application.State;
import fr.ill.ics.cameo.CancelEvent;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.Event;
import fr.ill.ics.cameo.EventListener;
import fr.ill.ics.cameo.OutputStreamSocket;
import fr.ill.ics.cameo.PortEvent;
import fr.ill.ics.cameo.PublisherEvent;
import fr.ill.ics.cameo.ResultEvent;
import fr.ill.ics.cameo.StatusEvent;
import fr.ill.ics.cameo.SubscriberCreationException;


/**
 * Class that implements simple asynchronous programming model.
 * There is no connection timeout as they are hidden as bad results.
 * The class is not thread safe and should be used in a single thread.
 * Question? stop/kill can be called concurrently 
 * @author legoc
 *
 */
public class InstanceImpl extends EventListener {

	private ServerImpl server;
	private int id = -1;
	private String errorMessage;
	private OutputStreamSocket outputSocket;
	private int pastStates = 0;
	private int initialState = Application.State.UNKNOWN;
	private int lastState = Application.State.UNKNOWN;
	private byte[] resultData;
	private InstanceWaitingImpl waiting = new InstanceWaitingImpl(this);	
	
	InstanceImpl(ServerImpl server) {
		this.server = server;
		
		// Register the waiting
		waiting.add();
	}

	void setId(int id) {
		this.id = id;
	}
	
	void setOutputStreamSocket(OutputStreamSocket outputSocket) {
		this.outputSocket = outputSocket;		
	}
	
	void setErrorMessage(String message) {
		errorMessage = message;
	}
	
	void setPastStates(int pastStates) {
		this.pastStates = pastStates;
	}
	
	void setInitialState(int state) {
		initialState = state;
		
		// It is important to set the last state at this point because it may be the state returned by the method now if no state was received.
		lastState = state;
	}
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getUrl() {
		return server.getUrl();
	}
	
	public String getEndpoint() {
		return server.getEndpoint();
	}
	
	public String getStatusEndpoint() {
		return server.getStatusEndpoint();
	}
	
	public String getNameId() {
		return name + "." + id;
	}
	
	public boolean hasResult() {
		return (resultData != null);
	}
	
	/**
	 * 
	 * @return true if the instance exists, i.e. the task is executed, otherwise false. 
	 */
	public boolean exists() {
		return (id != -1);
	}
	
	/**
	 * Returns the error message.
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * Returns the initial state of the application when the Instance is created.
	 * This is the value of the application state when the connect was requested.
	 * Otherwise the value is UNKNOWN when the Instance is created from a start, stop or kill request. 
	 * The value returned is not synchronized with the waitFor method. 
	 * @return
	 */
	public int getInitialState() {
		return initialState;
	}
		
	/**
	 * Requests the stop of the application.
	 * The stop is not blocking, so it must be followed by a call to waitFor to ensure the termination of the application.
	 * Or it can be called in parallel with waitFor.
	 * @return false if it does not succeed, the error message is then set.
	 */
	public boolean stop() {
		try {
			Response response = server.stopApplicationAsynchronously(id, false);
			
		} catch (ConnectionTimeout e) {
			errorMessage = e.getMessage();
			return false;
		}
		
		return true;		
	}
	
	/**
	 * Requests the kill of the application.
	 * The stop is not blocking, so it must be followed by a call to waitFor to ensure the termination of the application.
	 * Or it can be called in parallel with waitFor. 
	 * @return false if it does not succeed, the error message is then set.
	 */
	public boolean kill() {
		try {
			Response response = server.stopApplicationAsynchronously(id, true);
			
		} catch (ConnectionTimeout e) {
			errorMessage = e.getMessage();
			return false;
		}
		
		return true;		
	}
	
	@Override
	public void notifyTerminalState(int applicationId) {
		
		// Unregister here.
		if (applicationId == this.id) {
			terminate();
		}
	}
		
	/**
	 * The call is blocking until a terminal state is received i.e. SUCCESS, STOPPED, KILLED, ERROR.
	 * The method is not thread-safe and must not be called concurrently.
	 */
	public int waitFor(int states, String eventName, boolean blocking) {

		if (!exists()) {
			return lastState;
		}
		
		// test the terminal state
		if (lastState == Application.State.SUCCESS
				|| lastState == Application.State.STOPPED
				|| lastState == Application.State.KILLED
				|| lastState == Application.State.ERROR) {
			// the application is already terminated
			return lastState;
		}
		
		// test the requested states
		if ((states & pastStates) != 0) {
			// the state is already received
			return lastState;
		}
	
		while (true) {
			// waits for a new incoming status
			Event event = popEvent(blocking);
			
			// If the event is null, then it is the result of non-blocking call.
			if (event == null) {
				break;
			}
			
			if (event.getId() == id) {
			
				if (event instanceof StatusEvent) {
				
					StatusEvent status = (StatusEvent)event;
					int state = status.getState();
					pastStates = status.getPastStates();
					lastState = state;
					
					// test the terminal state
					if (state == Application.State.SUCCESS 
						|| state == Application.State.STOPPED
						|| state == Application.State.KILLED					
						|| state == Application.State.ERROR) {
						break;
					}
					
					// test the requested states
					if ((states & pastStates) != 0) {
						return lastState;
					}
					
				} else if (event instanceof ResultEvent) {
					
					ResultEvent result = (ResultEvent)event;
					resultData = result.getData();
					
				} else if (event instanceof PublisherEvent) {
					
					PublisherEvent publisher = (PublisherEvent)event;
					
					if (publisher.getPublisherName().equals(eventName)) {
						break;
					}
					
				} else if (event instanceof PortEvent) {
					
					PortEvent port = (PortEvent)event;
					
					if (port.getPortName().equals(eventName)) {
						break;
					}
					
				} else if (event instanceof CancelEvent) {
					break;
				}
			}	
		}
		
		return lastState;
	}
	
	public int waitFor(int states, String eventName) {
		return waitFor(states, eventName, true);
	}
	
	public int waitFor(int states) {
		return waitFor(states, null);
	}
	
	/**
	 * The call is blocking until a terminal state is received i.e. SUCCESS, STOPPED, KILLED, ERROR.
	 */
	public int waitFor() {
		return waitFor(0);
	}
	
	public void cancelWaitFor() {
		cancel(id);
	}

	public int getActualState() {
		List<Info> infos = server.getApplicationInfos(name);
		
		for (Info info : infos) {
			if (info.getId() == id) {
				return info.getApplicationState();
			}
		}

		return State.UNKNOWN;
	}
	
	public void terminate() {
		// Unregister the status.
		server.unregisterStatusListener(this);
		
		// Unregister the waiting.
		waiting.remove();
	}
	
	/**
	 * Subscribes to the application publisher.
	 * @param publisherName
	 * @return
	 */
	public SubscriberImpl subscribe(String publisherName) {
		try {
			SubscriberImpl subscriber = server.createSubscriber(id, publisherName, this);
			return subscriber;
			
		} catch (SubscriberCreationException e) {
			// the publisher does not exist, so we are waiting for it
		}
		
		// waiting for the publisher
		waitFor(0, publisherName);
		
		// state cannot be terminal or it means that the application has terminated that is not planned.
		if (lastState == Application.State.SUCCESS 
			|| lastState == Application.State.STOPPED
			|| lastState == Application.State.KILLED					
			|| lastState == Application.State.ERROR) {
			return null;
		}
		
		try {
			SubscriberImpl subscriber = server.createSubscriber(id, publisherName, this);
			return subscriber;
			
		} catch (SubscriberCreationException e) {
			// that should not happen
			System.err.println("the publisher " + publisherName + " does not exist but should");
		}
		
		return null;
	}
		
	/**
	 * Returns the result of the Instance.
	 * @return
	 */
	public byte[] getResult() {
		
		waitFor();
		return resultData;
	}
	
	public String getStringResult() {
		return Buffer.parseString(getResult());
	}
	
	public int[] getInt32Result() {
		return Buffer.parseInt32(getResult());
	}
	
	public long[] getInt64Result() {
		return Buffer.parseInt64(getResult());
	}
	
	public float[] getFloatResult() {
		return Buffer.parseFloat(getResult());
	}

	public double[] getDoubleResult() {
		return Buffer.parseDouble(getResult());
	}
	
	public OutputStreamSocket getOutputStreamSocket() {
		return outputSocket;
	}
	
	@Override
	public String toString() {
		return name + "." + id + "@" + server.getEndpoint();
	}

}