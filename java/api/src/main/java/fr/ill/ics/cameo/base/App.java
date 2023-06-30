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

package fr.ill.ics.cameo.base;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.AppIdentity;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.ServerIdentity;

/**
 * Class defining a remote Cameo application.
 *
 * An App instance is created by a Server instance. It represents a real remote application that was started by a real Cameo server.
 */
public class App extends EventListener {

	private Server server;
	private int id = -1;
	private OutputStreamSocket outputSocket;
	private int pastStates = 0;
	private int initialState = State.NIL;
	private int lastState = State.NIL;
	private byte[] resultData;
	private AppWaiting waiting = new AppWaiting(this);
	private Integer exitCode;
	
	/**
	 * Class defining the Communication Operations Manager (COM) for an App instance.
	 *
	 * It facilitates the definition of communication objects.
	 */
	public static class Com {
		
		private Server server;
		private int applicationId;
		private String name;
		
		Com(Server server, int applicationId, String name) {
			this.server = server;
			this.applicationId = applicationId;
			this.name = name;
		}

		/**
		 * Gets the responder proxy port.
		 * @return The port.
		 */
		public int getResponderProxyPort() {
			return server.getResponderProxyPort();
		}
		
		/**
		 * Gets the publisher proxy port.
		 * @return The port.
		 */
		public int getPublisherProxyPort() {
			return server.getPublisherProxyPort();
		}
		
		/**
		 * Gets the subscriber proxy port.
		 * @return The port.
		 */
		public int getSubscriberProxyPort() {
			return server.getSubscriberProxyPort();
		}
		
		/**
		 * Gets the key value.
		 * @param key The key.
		 * @return The value associated to key.
		 */
		public String getKeyValue(String key) throws UndefinedApplicationException, UndefinedKeyException {
			return server.getKeyValue(applicationId, key);
		}
		
		/**
		 * Class defining a getter for a key value.
		 */
		public static class KeyValueGetter extends EventListener {

			private Server server;
			private int id;
			private String key;
			private KeyValueGetterWaiting waiting = new KeyValueGetterWaiting(this);
			private AtomicBoolean canceled = new AtomicBoolean(false);

			KeyValueGetter(Server server, String name, int id, String key) {
				this.server = server;
				this.id = id;
				this.setName(name);
				this.key = key;
				
				server.registerEventListener(this);
			}
			
			/**
			 * Gets the value.
			 * @param timeoutCounter The timeout counter.
			 * @return The value.
			 * @throws KeyValueGetterException if the call has been canceled.
			 */
			public String get(TimeoutCounter timeoutCounter) throws KeyValueGetterException {
				
				// Register the waiting.
				waiting.add();
				
				try {
					try {
						return server.getKeyValue(id, key);
					}
					catch (UndefinedApplicationException e) {
						// The application has already terminated.
						throw new KeyValueGetterException("Application terminated");
					}
					catch (UndefinedKeyException e) {
						// Key is not found, waiting for the event.
					}
				
					while (true) {
						// Waits for a new incoming status. The call may throw a a timeout.
						int remainingTimeout = timeoutCounter.remains();
						Event event = popEvent(remainingTimeout);
												
						if (event.getId() == id) {
						
							if (event instanceof StatusEvent) {
							
								StatusEvent status = (StatusEvent)event;
								int state = status.getState();
								
								// Test the terminal state.
								if (state == State.SUCCESS 
									|| state == State.STOPPED
									|| state == State.KILLED					
									|| state == State.FAILURE) {
									throw new KeyValueGetterException("Application terminated");
								}
								
							}
							else if (event instanceof KeyEvent) {
								
								KeyEvent keyEvent = (KeyEvent)event;
			
								// Check if it is the event that is waited for.
								if (keyEvent.getKey().equals(key)) {
									
									// Set the status and value.
									if (keyEvent.getStatus() == KeyEvent.Status.STORED) {
										return keyEvent.getValue();
									}
									throw new KeyValueGetterException("Key removed");
								}
								
							}
							else if (event instanceof CancelEvent) {
								return "";
							}
						}	
					}
				}
				finally {
					// Unregister the waiting.
					waiting.remove();
					server.unregisterEventListener(this);
				}
			}
			
			/**
			 * Cancels the get call.
			 */
			public void cancel() {
				canceled.set(true);
				this.cancel(id);
			}
			
			/**
			 * Returns true if is canceled.
			 * @return True if is canceled.
			 */
			public boolean isCanceled() {
				return canceled.get();
			}
		}
		
		/**
		 * Creates a KeyValueGetter for a key.
		 * \return A new KeyValueGetter instance.
		 */
		public KeyValueGetter createKeyValueGetter(String key) {
			return new KeyValueGetter(server, name, applicationId, key);
		}
		
		/**
		 * Method provided by convenience to simplify the parsing of JSON messages.
		 * @param message The message to parse.
		 * @return A JSONObject object.
		 */
		public JSONObject parse(byte[] message) {
			try {
				return server.parse(message);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse message");
			}
		}
		
	}
	
	/**
	 * Class defining the configuration of a registered application.
	 */
	public static class Config {
	
		private String name;
		private String description;
		private int multiple;
		private boolean restart;
		private int startingTime;
		private int stoppingTime;
	
		/**
		 * Constructor.
		 * @param name The name.
		 * @param description The description.
		 * @param multiple The maximum number of running apps.
		 * @param restart True if the application can restart.
		 * @param startingTime Starting time in seconds.
		 * @param stoppingTime Stopping time in seconds.
		 */
		public Config(String name, String description, int multiple, boolean restart, int startingTime, int stoppingTime) {
			super();
			this.description = description;
			this.multiple = multiple;
			this.restart = restart;
			this.name = name;
			this.startingTime = startingTime;
			this.stoppingTime = stoppingTime;
		}

		/**
		 * Gets the name.
		 * @return The name.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Gets the description.
		 * @return The description.
		 */
		public String getDescription() {
			return description;
		}
		
		/**
		 * Returns the multiplicity of the application.
		 * @return The maximum number of running apps.
		 */
		public int getMultiple() {
			return multiple;
		}
	
		/**
		 * Returns true if the application can restart.
		 * @return True if the application can restart.
		 */
		public boolean canRestart() {
			return restart;
		}
	
		/**
		 * Returns the starting time.
		 * @return The starting time in seconds.
		 */
		public int getStartingTime() {
			return startingTime;
		}
	
		/**
		 * Returns the stopping time.
		 * @return The stopping time in seconds.
		 */
		public int getStoppingTime() {
			return stoppingTime;
		}
		
		@Override
		public String toString() {
			JSONObject result = new JSONObject();
			
			result.put("name", name);
			result.put("description", description);
			result.put("multiple", multiple);
			result.put("restart", restart);
			result.put("starting_time", startingTime);
			result.put("stopping_time", stoppingTime);
			
			return result.toJSONString();
		}
	
	}

	/**
	 * Class showing the information of a running Cameo application.
	 */
	public static class Info {
	
		private int id;
		private int applicationState;
		private int pastApplicationStates;
		private String args;
		private String name;
		private long pid;
	
		/**
		 * Constructor.
		 * @param name The name.
		 * @param id The Cameo id.
		 * @param pid The PID.
		 * @param applicationState The current application state.
		 * @param pastApplicationStates The past application states.
		 * @param args The arguments of the executable.
		 */
		public Info(String name, int id, long pid, int applicationState, int pastApplicationStates, String args) {
			super();
			this.id = id;
			this.pid = pid;
			this.applicationState = applicationState;
			this.pastApplicationStates = pastApplicationStates;
			this.args = args;
			this.name = name;
		}
	
		/**
		 * Gets the id.
		 * @return The id.
		 */
		public int getId() {
			return id;
		}
	
		/**
		 * Gets the state.
		 * @return The state.
		 */
		public int getState() {
			return applicationState;
		}
		
		/**
		 * Gets the past states.
		 * @return the past states.
		 */
		public int getPastStates() {
			return pastApplicationStates;
		}
	
		/**
		 * Gets the arguments of the executable.
		 * @return The arguments.
		 */
		public String getArgs() {
			return args;
		}
		
		/**
		 * Gets the name.
		 * @return The name.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Gets the PID of the process.
		 * @return The PID.
		 */
		public long getPid() {
			return pid;
		}
	
		@Override
		public String toString() {
			JSONObject result = new JSONObject();
			
			result.put("name", name);
			result.put("id", id);
			result.put("state", applicationState);
			result.put("past_states", pastApplicationStates);
			result.put("args", args);
			
			return result.toJSONString();
		}
	
	}

	/**
	 * Class defining a system port associated to a Cameo application.
	 */
	public static class Port {
		
		private int port;
		private String status;
		private String owner;
		
		/**
		 * Constructor.
		 * @param port The port.
		 * @param status The status.
		 * @param owner The owner.
		 */
		public Port(int port, String status, String owner) {
			super();
			this.port = port;
			this.status = status;
			this.owner = owner;
		}
	
		/**
		 * Gets the port.
		 * @return The port.
		 */
		public int getPort() {
			return port;
		}
	
		/**
		 * Gets the status.
		 * @return The status.
		 */
		public String getStatus() {
			return status;
		}
	
		/**
		 * Gets the owner.
		 * @return The owner.
		 */
		public String getOwner() {
			return owner;
		}
		
		@Override
		public String toString() {
			JSONObject result = new JSONObject();
			
			result.put("port", port);
			result.put("status", status);
			result.put("owner", owner);
			
			return result.toJSONString();
		}		
	}

	private Com com;
		
	
	App(Server server) {
		this.server = server;
	}
	
	void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the COM object providing a helper to write the communication classes.
	 * \return The Com object.
	 */
	public Com getCom() {
		if (com == null) {
			com = new Com(server, id, getName());
		}
		return com;
	}
	
	void setOutputStreamSocket(OutputStreamSocket outputSocket) {
		if (outputSocket != null) {
			this.outputSocket = outputSocket;
			this.outputSocket.setApplicationId(id);
		}
	}
	
	void setPastStates(int pastStates) {
		this.pastStates = pastStates;
	}
	
	void setInitialState(int state) {
		initialState = state;
		
		// It is important to set the last state at this point because it may be the state returned by the method now if no state was received.
		lastState = state;
	}
	
	/**
	 * Gets the id.
	 * @return the Cameo id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the use of the proxy.
	 * @return True if the proxy is used.
	 */
	public boolean usesProxy() {
		return server.usesProxy();
	}
	
	/**
	 * Gets the endpoint of the server running this remote application.
	 * @return The endpoint.
	 */
	public Endpoint getEndpoint() {
		return server.getEndpoint();
	}

	/**
	 * Gets the status endpoint of the server running this remote application.
	 * @return The endpoint.
	 */
	public Endpoint getStatusEndpoint() {
		return server.getStatusEndpoint();
	}
	
	/**
	 * Gets the string concatenation of the name and the id.
	 * @return the name id.
	 */
	public String getNameId() {
		return getName() + "." + id;
	}
	
	/**
	 * Returns true if a result was received from the remote application.
	 * @return True if there is a result.
	 */
	public boolean hasResult() {
		return (resultData != null);
	}

	/**
	 * Returns the exit code.
	 * @return The exit code.
	 */
	public Integer getExitCode() {
		return exitCode;
	}
	
	/**
	 * Returns the initial state of the application when the App is created.
	 * @return The initial state.
	 */
	public int getInitialState() {
		return initialState;
	}
	
	/**
	 * Stops the remote application.
	 * The call is not blocking, so it must be followed by a call to waitFor to ensure the termination of the application.
	 * @return True if the request succeeded.
	 */
	public boolean stop() {
		try {
			Response response = server.stop(id, false);
		}
		catch (ConnectionTimeout e) {
			return false;
		}
		
		return true;		
	}
	
	/**
	 * Kills the remote application.
	 * The call is not blocking, so it must be followed by a call to waitFor to ensure the termination of the application.
	 * @return True if the request succeeded.
	 */
	public boolean kill() {
		try {
			Response response = server.stop(id, true);
		}
		catch (ConnectionTimeout e) {
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
			if (state == State.SUCCESS 
					|| state == State.STOPPED
					|| state == State.KILLED
					|| state == State.FAILURE) {
				
				// Unregister here.
				if (status.getId() == this.id) {
					terminate();
				}
			}
		}
	}
		
	private int waitFor(int states, KeyValue keyValue, boolean blocking) {

		try {
			// Register the waiting.
			waiting.add();
		
			// Test the terminal state.
			if (lastState == State.SUCCESS
					|| lastState == State.STOPPED
					|| lastState == State.KILLED
					|| lastState == State.FAILURE) {
				// The application is already terminated.
				return lastState;
			}
			
			// Test the requested states.
			if ((states & pastStates) != 0) {
				// The state is already received.
				return lastState;
			}
		
			while (true) {
				// Wait for a new incoming status.
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
						
						// Test the terminal state.
						if (state == State.SUCCESS 
							|| state == State.STOPPED
							|| state == State.KILLED					
							|| state == State.FAILURE) {
							break;
						}
						
						// Test the requested states.
						if ((states & pastStates) != 0) {
							return lastState;
						}
						
					}
					else if (event instanceof ResultEvent) {
						
						ResultEvent result = (ResultEvent)event;
						resultData = result.getData();
						
					}
					else if (event instanceof KeyEvent) {
						
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
						
					}
					else if (event instanceof CancelEvent) {
						break;
					}
				}	
			}
			
			return lastState;
		}
		finally {
			// Unregister the waiting.
			waiting.remove();
		}
	}
	
	/**
	 * Waits for the termination of the application.
	 * The method is not thread-safe and must not be called concurrently.
	 * @param keyValue The key value. 
	 * @return The state when the call returned.
	 */
	public int waitFor(KeyValue keyValue) {
		return waitFor(0, keyValue, true);
	}
	
	/**
	 * Waits for the termination of the application.
	 * The method is not thread-safe and must not be called concurrently.
	 * @param states The states.
	 * @param blocking True if the call is blocking.
	 * @return The state when the call returned.
	 */
	public int waitFor(int states, boolean blocking) {
		return waitFor(states, null, blocking);
	}
	
	/**
	 * Waits for the termination of the application.
	 * The method is not thread-safe and must not be called concurrently.
	 * @param states The states.
	 * @return The state when the call returned.
	 */
	public int waitFor(int states) {
		return waitFor(states, null, true);
	}
	
	/**
	 * Waits for the termination of the application.
	 * The method is not thread-safe and must not be called concurrently.
	 * @return The state when the call returned.
	 */
	public int waitFor() {
		return waitFor(0);
	}

	/**
	 * Cancels the blocking waitFor() in another thread.
	 */
	public void cancel() {
		cancel(id);
	}

	/**
	 * Returns the current state or NIL if the instance does not exist anymore.
	 * @return The current state.
	 */
	public int getState() {
		return server.getState(id);
	}

	/**
	 * Returns the past states.
	 * @return The past states.
	 */
	public Set<Integer> getPastStates() {
		return server.getPastStates(id);
	}
	
	/**
	 * Gets the last state.
	 * @return The last state.
	 */
	public int getLastState() {
		return waitFor(0, null, false);
	}
	
	/**
	 * Terminates the communication. The object is not usable after this call.
	 */
	public void terminate() {
		// Unregister the status.
		server.unregisterEventListener(this);
	}
		
	/**
	 * Returns the result if there is one.
	 * @return The result that may not exist.
	 */
	public byte[] getResult() {
		waitFor();
		return resultData;
	}
	
	/**
	 * Returns the string result if there is one.
	 * @return The string result that may not exist.
	 */
	public String getStringResult() {
		byte[] result = getResult();
		if (result == null) {
			return null;
		}
		return Messages.parseString(result);
	}
		
	/**
	 * Gets the output stream socket.
	 * @return The output stream socket.
	 */
	public OutputStreamSocket getOutputStreamSocket() {
		return outputSocket;
	}
	
	@Override
	public String toString() {
		return new AppIdentity(getName(), id, new ServerIdentity(server.getEndpoint().toString(), server.usesProxy())).toString();
	}

}
