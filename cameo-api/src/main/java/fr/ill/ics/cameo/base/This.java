package fr.ill.ics.cameo.base;

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.ProcessHandlerImpl;
import fr.ill.ics.cameo.base.Application.Handler;
import fr.ill.ics.cameo.base.Application.State;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class This {
	
	static This impl;
	
	private Endpoint serverEndpoint;
	private String name;
	private int id = -1;
	private boolean managed = false;

	private Endpoint starterEndpoint;
	private String starterName;
	private int starterId;
	
	// Definition of a EventListener member.
	private EventListener eventListener = new EventListener();
	private HandlerRunner stopHandler;
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
		
		public void storeKeyValue(String key, String value) {
			server.storeKeyValue(applicationId, key, value);
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

		public JSONObject requestJSON(JSONObject request) {
			return server.requestJSON(request);
		}

		public RequestSocket createRequestSocket(String endpoint) {
			return server.createRequestSocket(endpoint);
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
		
		//TODO Remove when use of key values is done.
		public void removePort(String name) {
			
			JSONObject response = server.requestJSON(Messages.createRemovePortV0Request(applicationId, name));
				
			int port = JSON.getInt(response, Messages.RequestResponse.VALUE);
			if (port == -1) {
				System.err.println("Cannot remove port " + name);
			}
		}

	}
	
	private static Com com;
	
	void initServer() {
		server.registerEventListener(impl.getEventListener());
		
		if (impl.getStarterEndpoint() != null) {
			starterServer = new Server(impl.getStarterEndpoint());
		}
		com = new Com(impl.getServer(), impl.getId());
	}
	
	static public void init(String[] args) {
		impl = new This(args);
		impl.initServer();
	}
	
	static public void init(String name, String endpoint) {
		impl = new This(name, endpoint);
		impl.initServer();
	}
			
	static public String getName() {
		if (impl == null) {
			return null;		
		}
		return impl.name;
	}

	static public int getId() {
		if (impl == null) {
			return 0;		
		}
		return impl.id;
	}
	
	public int getTimeout() {
		if (impl == null) {
			return 0;		
		}
		return impl.server.getTimeout();
	}

	public void setTimeout(int timeout) {
		if (impl == null) {
			return;		
		}
		impl.server.setTimeout(timeout);
	}
	
	static public Endpoint getEndpoint() {
		if (impl == null) {
			return null;		
		}
		return impl.serverEndpoint;
	}
	
	static public Server getServer() {
		if (impl == null) {
			return null;		
		}
		return impl.server;
	}
	
	static public Server getStarterServer() {
		if (impl == null) {
			return null;		
		}
		return impl.starterServer;
	}
	
	static public Com getCom() {
		return com;
	}
	
	static public boolean isAvailable(int timeout) {
		if (impl == null) {
			return false;		
		}
		return impl.server.isAvailable(timeout);
	}
	
	static public boolean isAvailable() {
		return isAvailable(10000);
	}
	
	static public void cancelWaitings() {
		if (impl == null) {
			return;		
		}
		impl.waitingSet.cancelAll();
	}
	
	static public void terminate() {
		impl.terminateAll();
	}

	static public void setResult(byte[] data) {
		JSONObject response = impl.server.requestJSON(Messages.createSetResultRequest(getId()), data);
		
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
		JSONObject response = impl.server.requestJSON(request);
	
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
		return (impl.getState(getId()) == Application.State.STOPPING);
	}
	
	/**
	 * Sets the stop handler with stopping time that overrides the one that may be defined in the configuration of the server.
	 * @param handler
	 */
	static public void handleStop(final Handler handler, int stoppingTime) {
		impl.createStopHandler(handler, stoppingTime);
	}
	
	/**
	 * Sets the stop handler with default stopping time.
	 * @param handler
	 */
	static public void handleStop(final Handler handler) {
		handleStop(handler, -1);
	}
	
	/**
	 * 
	 * @return
	 */
	static public Instance connectToStarter(int options) {
		if (impl.starterServer == null) {
			return null;
		}
		
		// Iterate the instances to find the id
		List<Instance> instances = impl.starterServer.connectAll(impl.getStarterName(), 0);
		for (Instance i : instances) {
			if (i.getId() == impl.getStarterId()) {
				return i;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	static public Instance connectToStarter() {
		return connectToStarter(0);
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

		// Get the name present for both managed and unmanaged apps.
		name = JSON.getString(infoObject, Messages.ApplicationIdentity.NAME);

		// For managed apps, id is present in info.
		if (infoObject.containsKey(Messages.ApplicationIdentity.ID)) {
			managed = true;
			id = JSON.getInt(infoObject, Messages.ApplicationIdentity.ID);
		}
		else {
			managed = false;
		}
		
		// Get the starter info if it is present.
		if (infoObject.containsKey(Messages.ApplicationIdentity.STARTER)) {
			JSONObject starterObject = JSON.getObject(infoObject, Messages.ApplicationIdentity.STARTER);
			starterEndpoint = Endpoint.parse(JSON.getString(starterObject, Messages.ApplicationIdentity.SERVER));
			starterName = JSON.getString(starterObject, Messages.ApplicationIdentity.NAME);
			starterId = JSON.getInt(starterObject, Messages.ApplicationIdentity.ID);
		}		
		
		// Init.
		initApplication();
	}
	
	private This(String name, String endpoint) {
		
		// Get the server endpoint.
		serverEndpoint = Endpoint.parse(endpoint);
		
		// Get the name.
		this.name = name; 
		
		// This is de-facto an unmanaged application.		
		managed = false;
		
		// Init.
		initApplication();
	}
	
	private void initApplication() {

		// Create the server.
		server = new Server(serverEndpoint, 0);
		
		// Init the unmanaged application.
		if (!managed) {
			id = initUnmanagedApplication();
			
			if (id == -1) {
				throw new UnmanagedApplicationException("Maximum number of applications " + name + " reached");
			}
		}
		
		// Init listener.
		eventListener.setName(name);
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
		
		if (stopHandler != null) {
			stopHandler.terminate();
		}
		
		waitingSet.terminateAll();

		// Tell the cameo server that the application is terminated if it is unmanaged.
		if (!managed) {
			terminateUnmanagedApplication();
		}
		
		server.terminate();
		if (starterServer != null) {
			starterServer.terminate();
		}
	}
	
	private int initUnmanagedApplication() {
		
		// Get the pid.
		long pid = ProcessHandlerImpl.pid();
		
		JSONObject response = server.requestJSON(Messages.createAttachUnmanagedRequest(name, pid));
	
		return JSON.getInt(response, Messages.RequestResponse.VALUE);
	}
	
	private void terminateUnmanagedApplication() {
		server.requestJSON(Messages.createDetachUnmanagedRequest(id));
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
		
	/**
	 * Waits for the stop event.
	 */
	int waitForStop() {
				
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
	
	
	void terminateWaitForStop() {
		eventListener.cancel(id);		
	}
	
	/**
	 * 
	 */
	private void createStopHandler(Handler handler, int stoppingTime) {
		
		if (handler == null) {
			return;
		}
		
		// Notify the server.
		setStopHandler(stoppingTime);
		
		stopHandler = new HandlerRunner(this, handler);
		stopHandler.start();
	}
	

	private EventListener getEventListener() {
		return eventListener;
	}

	@Override
	public String toString() {
		return name + "." + id + "@" + serverEndpoint;
	}
	
	
	
}