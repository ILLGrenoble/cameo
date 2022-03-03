package fr.ill.ics.cameo.base;

import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

/**
 * Class that implements simple asynchronous programming model.
 * There is no connection timeout as they are hidden as bad results.
 * The class is not thread safe and should be used in a single thread.
 * Question? stop/kill can be called concurrently 
 * @author legoc
 *
 */
public class Instance extends EventListener {

	private Server server;
	private int id = -1;
	private OutputStreamSocket outputSocket;
	private String errorMessage;
	private int pastStates = 0;
	private int initialState = Application.State.UNKNOWN;
	private int lastState = Application.State.UNKNOWN;
	private byte[] resultData;
	private InstanceWaiting waiting = new InstanceWaiting(this);
	private Integer exitCode;
	
	/**
	 * Class defining the Communication Operations Manager (COM).
	 */
	public static class Com {
		
		private Server server;
		private int applicationId;
		
		Com(Server server, int applicationId) {
			this.server = server;
			this.applicationId = applicationId;
		}

		public int getResponderProxyPort() {
			return server.getResponderProxyPort();
		}
		
		public int getPublisherProxyPort() {
			return server.getPublisherProxyPort();
		}
		
		public int getSubscriberProxyPort() {
			return server.getSubscriberProxyPort();
		}
		
		public String getKeyValue(String key) throws UndefinedApplicationException, UndefinedKeyException {
			return server.getKeyValue(applicationId, key);
		}
		
		/**
		 * Method provided by convenience to simplify the parsing of JSON messages.
		 * @param message
		 * @return
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
	
	private Com com;
		
	
	Instance(Server server) {
		this.server = server;
	}

	void setId(int id) {
		this.id = id;
	}
	
	public Server getServer() {
		return server;
	}
	
	public Com getCom() {
		if (com == null) {
			com = new Com(server, id);
		}
		return com;
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
	
	public boolean usesProxy() {
		return server.usesProxy();
	}
	
	public Endpoint getEndpoint() {
		return server.getEndpoint();
	}

	public Endpoint getStatusEndpoint() {
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
	private int waitFor(int states, KeyValue keyValue, boolean blocking) {

		try {
			// Register the waiting.
			waiting.add();
		
			// Exit if the app does not exist.
			if (!exists()) {
				return lastState;
			}
			
			// Test the terminal state.
			if (lastState == Application.State.SUCCESS
					|| lastState == Application.State.STOPPED
					|| lastState == Application.State.KILLED
					|| lastState == Application.State.ERROR) {
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
						if (state == Application.State.SUCCESS 
							|| state == Application.State.STOPPED
							|| state == Application.State.KILLED					
							|| state == Application.State.ERROR) {
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
	
	public int waitFor(KeyValue keyValue) {
		return waitFor(0, keyValue, true);
	}
	
	public int waitFor(int states, boolean blocking) {
		return waitFor(states, null, blocking);
	}
	
	public int waitFor(int states) {
		return waitFor(states, null, true);
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
	
	public int getLastState() {
		return waitFor(0, null, false);
	}
	
	public void terminate() {
		// Unregister the status.
		server.unregisterEventListener(this);
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
		byte[] result = getResult();
		if (result == null) {
			return null;
		}
		return Messages.parseString(result);
	}
		
	public OutputStreamSocket getOutputStreamSocket() {
		return outputSocket;
	}
	
	@Override
	public String toString() {
		return getName() + "." + id + "@" + server.getEndpoint();
	}

}
