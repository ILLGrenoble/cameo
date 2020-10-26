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

import java.util.Set;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.CancelEvent;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.Event;
import fr.ill.ics.cameo.EventListener;
import fr.ill.ics.cameo.KeyEvent;
import fr.ill.ics.cameo.KeyValue;
import fr.ill.ics.cameo.OutputStreamSocket;
import fr.ill.ics.cameo.PortEvent;
import fr.ill.ics.cameo.PublisherEvent;
import fr.ill.ics.cameo.ResultEvent;
import fr.ill.ics.cameo.StatusEvent;
import fr.ill.ics.cameo.SubscriberCreationException;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.strings.Endpoint;


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
	private OutputStreamSocket outputSocket;
	
	private String errorMessage;
	private int pastStates = 0;
	private int initialState = Application.State.UNKNOWN;
	private int lastState = Application.State.UNKNOWN;
	private byte[] resultData;
	private InstanceWaitingImpl waiting = new InstanceWaitingImpl(this);
	private Integer exitCode;
	
	InstanceImpl(ServerImpl server) {
		this.server = server;
		
		// Register the waiting
		waiting.add();
	}

	void setId(int id) {
		this.id = id;
	}
	
	public ComImpl createCom() {
		return new ComImpl(server, id);
	}
	
	void setOutputStreamSocket(OutputStreamSocket outputSocket) {
		if (outputSocket != null) {
			this.outputSocket = outputSocket;
			this.outputSocket.setApplicationId(id);
		}
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
	
	public int getId() {
		return id;
	}
	
	public String getUrl() {
		return server.getUrl();
	}
	
	public Endpoint getEndpoint() {
		return server.getEndpoint();
	}
	
	public String getStatusEndpoint() {
		return server.getStatusEndpoint();
	}
	
	public String getNameId() {
		return getName() + "." + id;
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
	
	public Integer getExitCode() {
		return exitCode;
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
	public void pushEvent(Event event) {
		super.pushEvent(event);
		
		// In case of status event, we need to terminate the instance to ensure to release the zeromq resources.
		if (event instanceof StatusEvent) {
		
			StatusEvent status = (StatusEvent)event;
			int state = status.getState();

			// Test if the state is terminal.
			if (state == Application.State.SUCCESS 
					|| state == Application.State.STOPPED
					|| state == Application.State.KILLED
					|| state == Application.State.ERROR) {
				
				// Unregister here.
				if (status.getId() == this.id) {
					terminate();
				}
			}
		}
	}
		
	/**
	 * The call is blocking until a terminal state is received i.e. SUCCESS, STOPPED, KILLED, ERROR.
	 * The method is not thread-safe and must not be called concurrently.
	 */
	private int waitFor(int states, String eventName, KeyValue keyValue, boolean blocking) {

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
					
					// Assign the exit code.
					if (status.getExitCode() != null) {
						exitCode = status.getExitCode();
					}
					
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
					
				} else if (event instanceof KeyEvent) {
					
					KeyEvent keyEvent = (KeyEvent)event;

					// Check if it is the event that is waited for.
					if (keyValue != null && keyEvent.getKey().equals(keyValue.getKey())) {
						
						// Set the status and value.
						if (keyEvent.getStatus() == KeyEvent.Status.STORED) {
							keyValue.setStatus(KeyValue.Status.STORED);
						}
						else {
							keyValue.setStatus(KeyValue.Status.REMOVED);
						}
						keyValue.setValue(keyEvent.getValue());
						break;
					}
					
				} else if (event instanceof CancelEvent) {
					break;
				}
			}	
		}
		
		return lastState;
	}
	
	public int waitFor(String eventName) {
		return waitFor(0, eventName, null, true);
	}
	
	public int waitFor(KeyValue keyValue) {
		return waitFor(0, null, keyValue, true);
	}
	
	public int waitFor(int states, boolean blocking) {
		return waitFor(states, null, null, blocking);
	}
	
	public int waitFor(int states) {
		return waitFor(states, null, null, true);
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
		return server.getActualState(id);
	}

	public Set<Integer> getPastStates() {
		return server.getPastStates(id);
	}
	
	public void terminate() {
		// Unregister the status.
		server.unregisterEventListener(this);
		
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
		waitFor(publisherName);
		
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
		return Message.parseString(getResult());
	}
		
	public OutputStreamSocket getOutputStreamSocket() {
		return outputSocket;
	}
	
	@Override
	public String toString() {
		return getName() + "." + id + "@" + server.getEndpoint();
	}

}