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

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.ProcessHandlerImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

/**
 * Class managing the current Cameo application.
 *
 * The application can be launched by the Cameo console or another Cameo App.
 */
public class This {
	
	static This instance;
	
	private Endpoint serverEndpoint;
	private String name;
	private int id = -1;
	private boolean registered = false;

	private Endpoint starterEndpoint;
	private String starterName;
	private int starterId;
	private int starterProxyPort;
	private boolean starterLinked;
	
	// Definition of a EventListener member.
	private EventListener eventListener = new EventListener();
	private WaitingSet waitingSet = new WaitingSet();
	
	private Server server;
	private Server starterServer;
	
	/**
	 * Class defining the Communication Operations Manager (COM) for this application.
	 *
	 * It facilitates the definition of communication objects.
	 */
	public static class Com {
		
		private Server server;
		private int applicationId;
		
		Com(Server server, int applicationId) {
			this.server = server;
			this.applicationId = applicationId;
		}

		/**
		 * Gets the communication context. Shall be a ZeroMQ context i.e. a ContextZmq instance.
		 * @return The context.
		 */
		public Context getContext() {
			return server.getContext();
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
		 * Stores the key value in the Cameo server.
		 * @param key The key.
		 * @param value The value.
		 * @throws KeyAlreadyExistsException if the key is already stored.
		 */
		public void storeKeyValue(String key, String value) throws KeyAlreadyExistsException {
			try {
				server.storeKeyValue(applicationId, key, value);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
		}
		
		/**
		 * Gets the key value from the Cameo server.
		 * @param key The key.
		 * @return The value associated to key.
		 * @throws UndefinedKeyException if the key is not found.
		 */
		public String getKeyValue(String key) throws UndefinedKeyException {
			try {
				return server.getKeyValue(applicationId, key);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
			return null;
		}
		
		/**
		 * Removes the key from the Cameo server.
		 * @param key The key.
		 * @throws UndefinedKeyException if the key is not found.
		 */
		public void removeKey(String key) throws UndefinedKeyException {
			try {
				server.removeKey(applicationId, key);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
		}
		
		/**
		 * Requests a new port from the Cameo server.
		 * @return An available port.
		 */
		public int requestPort() {
			try {
				return server.requestPort(applicationId);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
			return -1;
		}
		
		/**
		 * Tells the Cameo server that the port is not availaible i.e. another application onws it.
		 * @param port The port.
		 */
		public void setPortUnavailable(int port) {
			try {
				server.setPortUnavailable(applicationId, port);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
		}
		
		/**
		 * Releases the port so that the Cameo server will be able to return it in a future request.
		 * @param port The port.
		 */
		public void releasePort(int port) {
			try {
				server.releasePort(applicationId, port);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
		}

		/**
		 * Creates a request socket.
		 * @return A new request socket.
		 */
		public RequestSocket createRequestSocket(String endpoint, String responderIdentity) {
			return server.createRequestSocket(endpoint, responderIdentity);
		}
		
		/**
		 * Creates a request socket with a timeout.
		 * @return A new request socket.
		 */
		public RequestSocket createRequestSocket(String endpoint, String responderIdentity, int timeout) {
			return server.createRequestSocket(endpoint, responderIdentity, timeout);
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
		
		/**
		 * Method provided by convenience to simplify the parsing of JSON messages.
		 * @param message The message to parse.
		 * @return A JSONObject object.
		 */
		public JSONObject parse(String message) {
		
			try {
				return server.parse(message);
			}
			catch (ParseException e) {
				throw new UnexpectedException("Cannot parse message");
			}
		}
	}
	
	private static Com com;
	
	private Handler stopHandler;
	private Thread checkStatesThread = null;
	
	/**
	 * Initializes this application from the main arguments.
	 * @param args The program arguments.
	 */
	static public void init(String[] args) {
		instance = new This(args);
	}
	
	/**
	 * Initializes this application with direct parameters.
	 * @param name The Cameo name.
	 * @param endpoint The Cameo server endpoint e.g. tcp://myhost:7000.
	 */
	static public void init(String name, String endpoint) {
		instance = new This(name, endpoint);
	}
	
	/**
	 * Returns the Cameo name of this application.
	 * @return The Cameo name.
	 */
	static public String getName() {
		if (instance == null) {
			return null;		
		}
		return instance.name;
	}

	/**
	 * Returns the Cameo id of this application.
	 * @return The Cameo id.
	 */
	static public int getId() {
		if (instance == null) {
			return 0;		
		}
		return instance.id;
	}
	
	/**
	 * Gets the timeout.
	 * @return The timeout value.
	 */
	public int getTimeout() {
		if (instance == null) {
			return 0;		
		}
		return instance.server.getTimeout();
	}

	/**
	 * Sets the timeout.
	 * @param value The timeout value.
	 */
	public void setTimeout(int timeout) {
		if (instance == null) {
			return;		
		}
		instance.server.setTimeout(timeout);
	}
	
	/**
	 * Returns the endpoint of the Cameo server.
	 * @return The Cameo endpoint.
	 */
	static public Endpoint getEndpoint() {
		if (instance == null) {
			return null;		
		}
		return instance.serverEndpoint;
	}
	
	/**
	 * Returns the Cameo server that owns this application.
	 * @return The Server instance.
	 */
	static public Server getServer() {
		if (instance == null) {
			return null;		
		}
		return instance.server;
	}
	
	/**
	 * Returns the COM object.
	 * @return The Com object.
	 */
	static public Com getCom() {
		return com;
	}
	
	/**
	 * Returns true if the Cameo server that owns this application is available.
	 * @param timeout The timeout value.
	 * @return True if the Cameo replies within the timeout.
	 */
	static public boolean isAvailable(int timeout) {
		if (instance == null) {
			return false;		
		}
		return instance.server.isAvailable(timeout);
	}
	
	/**
	 * Returns true if the Cameo server that owns this application is available.
	 * @return True if the Cameo replies within the timeout.
	 */
	static public boolean isAvailable() {
		return isAvailable(10000);
	}
	
	/**
	 * Cancels all the waiting calls.
	 */
	static public void cancelAll() {
		if (instance == null) {
			return;
		}
		instance.waitingSet.cancelAll();
	}
	
	/**
	 * Terminates the application.
	 */
	static public void terminate() {
		if (instance == null) {
			return;
		}
		instance.terminateAll();
	}

	/**
	 * Sets the result.
	 * @param data The string result.
	 */
	static public void setResult(byte[] data) {
		JSONObject response = instance.server.requestJSON(Messages.createSetResultRequest(getId()), data);
		
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			throw new UnexpectedException("Cannot set result");
		}
	}
	
	/**
	 * Sets the string result.
	 * @param data The string result.
	 */
	static public void setStringResult(String data) {
		setResult(Messages.serialize(data));
	}
			
	/**
	 * Sets this application in RUNNING state.
	 * @return True or false.
	 */
	static public boolean setRunning() {
		JSONObject request = Messages.createSetStatusRequest(getId(), State.RUNNING);
		JSONObject response = instance.server.requestJSON(request);
	
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if the application is in STOPPING state.
	 * @return True or false.
	 */
	static public boolean isStopping() {
		return (instance.getState(getId()) == State.STOPPING);
	}
	
	/**
	 * Sets the stop handler with stopping time that overrides the one that may be defined in the
	 * configuration of the server.
	 * @param handler The stop handler.
	 * @param stoppingTime The stopping time in milliseconds.
	 */
	static public void handleStop(final Handler handler, int stoppingTime) {
		instance.initStopCheck(handler, stoppingTime);
	}
	
	/**
	 * Sets the stop handler with stopping time that overrides the one that may be defined in the
	 * configuration of the server.
	 * @param handler The stop handler.
	 */
	static public void handleStop(final Handler handler) {
		handleStop(handler, -1);
	}
	
	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 * The server and instance are returned. Be careful, the instance is linked to the server, so it must not be destroyed before.
	 * @param options The options passed to connect the starter app.
	 * @param useProxy True if the proxy is used to connect to the starter app.
	 * @param timeout Timeout for the server initialization.
	 */
	static public ServerAndApp connectToStarter(int options, boolean useProxy, int timeout) {
		
		if (instance.getStarterEndpoint() == null) {
			return null;
		}
		
		// Create the server with proxy or not.
		Server starterServer = null;
		App starterInstance = null;
		
		if (useProxy) {
			starterServer = Server.create(instance.getStarterEndpoint().withPort(instance.starterProxyPort), true);
		}
		else {
			starterServer = Server.create(instance.getStarterEndpoint(), false);
		}
		
		starterServer.setTimeout(timeout);
		
		try {
			starterServer.init();
			
			// Iterate the instances to find the id
			List<App> instances = starterServer.connectAll(instance.getStarterName(), options);
			for (App i : instances) {
				if (i.getId() == instance.getStarterId()) {
					starterInstance = i;
					break;
				}
			}
			
			if (starterInstance == null) {
				return null;
			}
		}
		catch (ConnectionTimeout e) {
			// Timeout while initializing the server.
		}
			
		return new ServerAndApp(starterServer, starterInstance);
	}
	
	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 * The server and instance are returned. Be careful, the instance is linked to the server, so it must not be destroyed before.
	 * @param options The options passed to connect the starter app.
	 * @param useProxy True if the proxy is used to connect to the starter app.
	 */
	static public ServerAndApp connectToStarter(int options, boolean useProxy) {
		return connectToStarter(options, useProxy, 0);
	}
	
	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 * The server and instance are returned. Be careful, the instance is linked to the server, so it must not be destroyed before.
	 * @param options The options passed to connect the starter app.
	 */
	static public ServerAndApp connectToStarter(int options) {
		return connectToStarter(options, false, 0);
	}

	/**
	 * Connects to the starter application, i.e. the application which started this application.
	 * The server and instance are returned. Be careful, the instance is linked to the server, so it must not be destroyed before.
	 */
	static public ServerAndApp connectToStarter() {
		return connectToStarter(0, false, 0);
	}
	
	private This(String[] args) {
		
		// Analyse the args to get the info.
		if (args.length == 0) {
			throw new InvalidArgumentException("Missing info argument");			
		}
		
		// The last argument contains the necessary information.
		String info = args[args.length - 1];
		
		// Parse the info.
		JSONObject infoObject;
		JSONParser parser = new JSONParser();
		
		try {
			infoObject = (JSONObject)parser.parse(info);
		}
		catch (ParseException e) {
			throw new InvalidArgumentException("Bad format for info argument");
		}

		// Get the server endpoint.
		serverEndpoint = Endpoint.parse(JSON.getString(infoObject, Messages.ApplicationIdentity.SERVER));

		// Get the name present for both registered and unregistered apps.
		name = JSON.getString(infoObject, Messages.ApplicationIdentity.NAME);

		// For registered apps, id is present in info.
		if (infoObject.containsKey(Messages.ApplicationIdentity.ID)) {
			registered = true;
			id = JSON.getInt(infoObject, Messages.ApplicationIdentity.ID);
		}
		else {
			registered = false;
		}
		
		// Get the starter info if it is present.
		if (infoObject.containsKey(Messages.ApplicationIdentity.STARTER)) {
			JSONObject starterObject = JSON.getObject(infoObject, Messages.ApplicationIdentity.STARTER);
			starterEndpoint = Endpoint.parse(JSON.getString(starterObject, Messages.ApplicationIdentity.SERVER));
			starterName = JSON.getString(starterObject, Messages.ApplicationIdentity.NAME);
			starterId = JSON.getInt(starterObject, Messages.ApplicationIdentity.ID);
			starterProxyPort = JSON.getInt(infoObject, Messages.ApplicationIdentity.STARTER_PROXY_PORT);
			starterLinked = JSON.getBoolean(infoObject, Messages.ApplicationIdentity.STARTER_LINKED);
		}		
		
		// Init.
		initApplication();
	}
	
	private This(String name, String endpoint) {
		
		// Get the server endpoint.
		serverEndpoint = Endpoint.parse(endpoint);
		
		// Get the name.
		this.name = name; 
		
		// This is de-facto an unregistered application.		
		registered = false;
		
		// Init.
		initApplication();
	}
	
	private void initApplication() {

		// Create the server.
		server = Server.create(serverEndpoint, false);
		server.init();
		
		// Init the unregistered application.
		if (!registered) {
			id = initUnregisteredApplication();
			
			if (id == -1) {
				throw new UnregisteredApplicationException("Maximum number of applications " + name + " reached");
			}
		}
		
		// Init listener.
		eventListener.setName(name);
		server.registerEventListener(eventListener);
		
		// Init starter check.
		if (starterLinked) {
			initStarterCheck();
		}
		
		com = new Com(server, id);
	}
		
	private Endpoint getStarterEndpoint() {
		return starterEndpoint;
	}
	
	private String getStarterName() {
		return starterName;
	}
	
	private int getStarterId() {
		return starterId;
	}
	
	WaitingSet getWaitingSet() {
		return waitingSet;
	}

	private void terminateAll() {
		
		waitingSet.terminateAll();

		// Tell the cameo server that the application is terminated if it is unregistered.
		if (!registered) {
			terminateUnregisteredApplication();
		}
		
		// Stop the check states thread.
		if (checkStatesThread != null) {
			
			// Cancel the listener.
			eventListener.cancel(id);

			try {
				checkStatesThread.join();
			}
			catch (InterruptedException e) {
			}
		}
		
		// Terminate the server.
		server.terminate();
		
		// Terminate the starter server if the application is linked.
		if (starterServer != null) {
			starterServer.terminate();
		}
	}
	
	private int initUnregisteredApplication() {
		
		// Get the pid.
		long pid = ProcessHandlerImpl.pid();
		
		JSONObject response = server.requestJSON(Messages.createAttachUnregisteredRequest(name, pid));
	
		return JSON.getInt(response, Messages.RequestResponse.VALUE);
	}
	
	private void terminateUnregisteredApplication() {
		server.requestJSON(Messages.createDetachUnregisteredRequest(id));
	}
	
	private void setStopHandler(int stoppingTime) {
		server.requestJSON(Messages.createSetStopHandlerRequest(id, stoppingTime));
	}
	
	/**
	 * Gets the application state.
	 * @return
	 * @throws ConnectionTimeout
	 */
	private int getState(int id) {
		
		JSONObject response = server.requestJSON(Messages.createGetStatusRequest(id));
			
		return JSON.getInt(response, Messages.StatusEvent.APPLICATION_STATE);
	}

	private void startCheckStatesThread() {
		
		if (checkStatesThread == null) {
			checkStatesThread = new Thread(new Runnable() {
				public void run() {
					// Warning, this method is executed in a parallel thread.
					int state = State.NIL; 
					
					while (true) {
						// waits for a new incoming status
						Event event = eventListener.popEvent();
						
						// Filter events coming from this.
						if (event.getId() == id) {
						
							if (event instanceof StatusEvent) {
							
								StatusEvent status = (StatusEvent)event;
								state = status.getState();
													
								if (state == State.STOPPING) {
									if (stopHandler != null) {
										stopHandler.handle();
									}
									return;
								}
							}
							else if (event instanceof CancelEvent) {
								break;
							}
						}
						
						// Filter events coming from starter.
						if (event.getId() == starterId) {
							
							if (event instanceof StatusEvent) {
								
								StatusEvent status = (StatusEvent)event;
								state = status.getState();

								// Stop this application if it was linked.
								if (state == State.STOPPED || state == State.KILLED || state == State.SUCCESS || state == State.FAILURE) {
									stop();
								}
							}
						}
					}
				}
			});
			
			checkStatesThread.start();
		}
	}
	
	private void initStopCheck(Handler handler, int stoppingTime) {
		
		if (handler == null) {
			return;
		}
		
		// Memorize handler.
		stopHandler = handler;
		
		// Notify the server.
		setStopHandler(stoppingTime);
		
		// Start the check states thread.
		startCheckStatesThread();
	}
	
	private void initStarterCheck() {
		
		// Create the starter server.
		// If the starter has a running proxy, then use the proxy: it is reasonable.
		if (starterProxyPort != 0) {
			starterServer = Server.create(starterEndpoint.withPort(starterProxyPort), true);
		}
		else {
			starterServer = Server.create(starterEndpoint, false);
		}
		
		starterServer.init();

		// Register this as event listener.
		starterServer.registerEventListener(eventListener, false);
		
		// Get the actual state. It is necessary to get the actual state after the registration so that we do not miss any events.
		int state = starterServer.getActualState(starterId);

		// Stop this app if the starter is already terminated i.e. the state is NIL.
		if (state == State.NIL) {
			stop();
		}
		else {
			startCheckStatesThread();
		}
	}
	
	private void stop() {
		
		// Use a request socket to avoid any race condition.
		RequestSocket requestSocket = server.createServerRequestSocket();
		
		JSONObject request = Messages.createStopRequest(id, true);
		requestSocket.requestJSON(request);
	}

	@Override
	public String toString() {
		return name + "." + id + "@" + serverEndpoint;
	}
	
}