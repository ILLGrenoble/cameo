package fr.ill.ics.cameo.base;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.ProcessHandlerImpl;
import fr.ill.ics.cameo.base.Application.Handler;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

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
	
	public static class Com {
		
		private Server server;
		private int applicationId;
		
		Com(Server server, int applicationId) {
			this.server = server;
			this.applicationId = applicationId;
		}

		public Context getContext() {
			return server.getContext();
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
		
		public void storeKeyValue(String key, String value) throws KeyAlreadyExistsException {
			try {
				server.storeKeyValue(applicationId, key, value);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
		}
		
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
		
		public void removeKey(String key) throws UndefinedKeyException {
			try {
				server.removeKey(applicationId, key);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
		}
		
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
		
		public void setPortUnavailable(int port) {
			try {
				server.setPortUnavailable(applicationId, port);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
		}
		
		public void releasePort(int port) {
			try {
				server.releasePort(applicationId, port);
			}
			catch (UndefinedApplicationException e) {
				// Should not happen in This.
				e.printStackTrace();
			}
		}

		public RequestSocket createRequestSocket(String endpoint, String responderIdentity) {
			return server.createRequestSocket(endpoint, responderIdentity);
		}
		
		public RequestSocket createRequestSocket(String endpoint, String responderIdentity, int timeout) {
			return server.createRequestSocket(endpoint, responderIdentity, timeout);
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
	
	static public void init(String[] args) {
		instance = new This(args);
	}
	
	static public void init(String name, String endpoint) {
		instance = new This(name, endpoint);
	}
			
	static public String getName() {
		if (instance == null) {
			return null;		
		}
		return instance.name;
	}

	static public int getId() {
		if (instance == null) {
			return 0;		
		}
		return instance.id;
	}
	
	public int getTimeout() {
		if (instance == null) {
			return 0;		
		}
		return instance.server.getTimeout();
	}

	public void setTimeout(int timeout) {
		if (instance == null) {
			return;		
		}
		instance.server.setTimeout(timeout);
	}
	
	static public Endpoint getEndpoint() {
		if (instance == null) {
			return null;		
		}
		return instance.serverEndpoint;
	}
	
	static public Server getServer() {
		if (instance == null) {
			return null;		
		}
		return instance.server;
	}
		
	static public Com getCom() {
		return com;
	}
	
	static public boolean isAvailable(int timeout) {
		if (instance == null) {
			return false;		
		}
		return instance.server.isAvailable(timeout);
	}
	
	static public boolean isAvailable() {
		return isAvailable(10000);
	}
	
	static public void cancelWaitings() {
		if (instance == null) {
			return;
		}
		instance.waitingSet.cancelAll();
	}
	
	static public void terminate() {
		instance.terminateAll();
	}

	static public void setResult(byte[] data) {
		JSONObject response = instance.server.requestJSON(Messages.createSetResultRequest(getId()), data);
		
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			throw new UnexpectedException("Cannot set result");
		}
	}
	
	static public void setResult(String data) {
		setResult(Messages.serialize(data));
	}
			
	/**
	 * Sets the owner application RUNNING.
	 * @return
	 * @throws StateException, ConnectionTimeout
	 */
	static public boolean setRunning() {
		JSONObject request = Messages.createSetStatusRequest(getId(), Application.State.RUNNING);
		JSONObject response = instance.server.requestJSON(request);
	
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if the application is in STOPPING state. Can be used when the application is already polling.
	 * @return
	 */
	static public boolean isStopping() {
		return (instance.getState(getId()) == Application.State.STOPPING);
	}
	
	/**
	 * Sets the stop handler with stopping time that overrides the one that may be defined in the configuration of the server.
	 * @param handler
	 */
	static public void handleStop(final Handler handler, int stoppingTime) {
		instance.initStopCheck(handler, stoppingTime);
	}
	
	/**
	 * Sets the stop handler with default stopping time.
	 * @param handler
	 */
	static public void handleStop(final Handler handler) {
		handleStop(handler, -1);
	}
	
	/**
	 * The server and instance are returned. Be careful, the instance is linked to the server, so it must not be destroyed before.
	 * @return
	 */
	static public ServerAndInstance connectToStarter(int options, boolean useProxy) {
		
		if (instance.getStarterEndpoint() == null) {
			return null;
		}
		
		// Create the server with proxy or not.
		Server starterServer;
		
		if (useProxy) {
			starterServer = new Server(instance.getStarterEndpoint().withPort(instance.starterProxyPort), 0, true);
		}
		else {
			starterServer = new Server(instance.getStarterEndpoint(), 0, false);	
		}
		
		// Iterate the instances to find the id
		Instance starterInstance = null;
		List<Instance> instances = starterServer.connectAll(instance.getStarterName(), options);
		for (Instance i : instances) {
			if (i.getId() == instance.getStarterId()) {
				starterInstance = i;
				break;
			}
		}
		
		if (starterInstance == null) {
			return null;
		}
		
		return new ServerAndInstance(starterServer, starterInstance);
	}
	
	/**
	 * 
	 * @return
	 */
	static public ServerAndInstance connectToStarter(int options) {
		return connectToStarter(options, false);
	}
	
	/**
	 * 
	 * @return
	 */
	static public ServerAndInstance connectToStarter() {
		return connectToStarter(0, false);
	}
	
	
	/**
	 * Constructor with application arguments.
	 * This constructor must be used when the services are related to the cameo server that
	 * has started the current application.
	 * Some methods may throw the runtime ConnectionTimeout exception, so it is recommended to catch the exception at a global scope if a timeout is set.
	 * @param args
	 */
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
		server = new Server(serverEndpoint, 0, false);
		
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
					int state = Application.State.UNKNOWN; 
					
					while (true) {
						// waits for a new incoming status
						Event event = eventListener.popEvent();
						
						// Filter events coming from this.
						if (event.getId() == id) {
						
							if (event instanceof StatusEvent) {
							
								StatusEvent status = (StatusEvent)event;
								state = status.getState();
													
								if (state == Application.State.STOPPING) {
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
								if (state == Application.State.STOPPED || state == Application.State.KILLED || state == Application.State.SUCCESS || state == Application.State.ERROR) {
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
		starterServer = new Server(starterEndpoint, 0, false);

		// Get the actual state.
		int state = starterServer.getActualState(starterId);
		starterServer.registerEventListener(eventListener, false);

		// Stop this app if the starter is already terminated i.e. the state is UNKNOWN.
		if (state == Application.State.UNKNOWN) {
			stop();
		}
		else {
			startCheckStatesThread();
		}
	}
	
	private void stop() {
		server.stop(id, false);
	}

	@Override
	public String toString() {
		return name + "." + id + "@" + serverEndpoint;
	}
	
	
	
}