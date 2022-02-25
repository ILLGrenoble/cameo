package fr.ill.ics.cameo.base;
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



import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.base.Application.State;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.StringId;

/**
 * The server class is thread-safe except for the connect and terminate methods that must be called respectively 
 * before and after any concurrent calls.
 * @author legoc
 *
 */
public class Server {

	private Endpoint serverEndpoint;
	private boolean useProxy;
	private int[] serverVersion = new int[3];
	private int publisherProxyPort;
	private int subscriberProxyPort;
	private int serverStatusPort;
	private int statusPort;
	private Context contextImpl;
	private int timeout = 0; // default value because of ZeroMQ design
	private RequestSocket requestSocket;
	private JSON.Parser parser = new JSON.Parser();
	private ConcurrentLinkedDeque<EventListener> eventListeners = new ConcurrentLinkedDeque<EventListener>(); 
	private EventThread eventThread;
	
	/**
	 * Constructor with endpoint.
	 * This constructor must be used when the services are related to another cameo server that
	 * has not started the current application.
	 * Some methods may throw the runtime ConnectionTimeout exception, so it is recommended to catch the exception at a global scope if a timeout is set. 
	 * @param endpoint
	 */
	public Server(Endpoint endpoint, int timeout, boolean useProxy) {
		this.useProxy = useProxy;
		this.initServer(endpoint, timeout);
	}
	
	public Server(Endpoint endpoint, int timeout) {
		this.useProxy = true;
		this.initServer(endpoint, timeout);
	}
	
	public Server(Endpoint endpoint) {
		this.useProxy = true;
		this.initServer(endpoint, 0);	
	}
	
	public Server(String endpoint, int timeout, boolean useProxy) {
		this.useProxy = useProxy;
		try {
			this.initServer(Endpoint.parse(endpoint), timeout);
		}
		catch (Exception e) {
			throw new InvalidArgumentException(endpoint + " is not a valid endpoint");
		}
	}
	
	public Server(String endpoint, int timeout) {
		try {
			this.initServer(Endpoint.parse(endpoint), timeout);
		}
		catch (Exception e) {
			throw new InvalidArgumentException(endpoint + " is not a valid endpoint");
		}
	}
	
	public Server(String endpoint) {
		try {
			this.initServer(Endpoint.parse(endpoint), 0);
		}
		catch (Exception e) {
			throw new InvalidArgumentException(endpoint + " is not a valid endpoint");
		}
	}
	
	private void initServer(Endpoint endpoint, int timeout) {
		
		this.timeout = timeout;
		
		serverEndpoint = endpoint;
				
		// Init the context and socket.
		init();
		
		// Retrieve the server version.
		retrieveServerVersion();
		
		// Start the status thread if it is possible.
		EventStreamSocket streamSocket = openEventStream();
		
		if (streamSocket != null) {
			eventThread = new EventThread(this, streamSocket);
			eventThread.start();
		}
	}
	
	/**
	 * Initializes the context and the request socket. The serverEndpoint must have been set.
	 */
	final private void init() {
		contextImpl = ImplFactory.createContext();
		requestSocket = this.createRequestSocket(serverEndpoint.toString(), StringId.CAMEO_SERVER);
		
		// Get the status port.
		serverStatusPort = retrieveStatusPort();
		
		// Get the publisher and subscriber proxy ports.
		publisherProxyPort = retrievePublisherProxyPort();
		subscriberProxyPort = retrieveSubscriberProxyPort();
	}
	
	private int getAvailableTimeout() {
		int timeout = getTimeout();
		if (timeout > 0) {
			return timeout;
		}
		
		return 10000;
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public Endpoint getEndpoint() {
		return serverEndpoint;
	}
		
	public int[] getVersion() {
		return serverVersion;
	}
	
	public Endpoint getStatusEndpoint() {
		return serverEndpoint.withPort(statusPort);
	}
	
	Context getContext() {
		return contextImpl;
	}
	
	int getPublisherProxyPort() {
		return publisherProxyPort;
	}
	
	int getSubscriberProxyPort() {
		return subscriberProxyPort;
	}
			
	JSONObject parse(byte[] data) throws ParseException {
		return parser.parse(Messages.parseString(data));
	}
	
	JSONObject parse(String data) throws ParseException {
		return parser.parse(data);
	}
	
	/**
	 * test connection with server
	 * @param timeout
	 * 
	 */
	private boolean isConnected(int timeout) {

		try {
			requestSocket.requestJSON(Messages.createSyncRequest(), timeout);
			return true;

		} catch (ConnectionTimeout e) {
			// do nothing, timeout
		} catch (Exception e) {
			// do nothing
		}
		
		return false;
	}

	private void retrieveServerVersion() {
		
		JSONObject response = requestSocket.requestJSON(Messages.createVersionRequest());
		
		serverVersion[0] = JSON.getInt(response, Messages.VersionResponse.MAJOR);
		serverVersion[1] = JSON.getInt(response, Messages.VersionResponse.MINOR);
		serverVersion[2] = JSON.getInt(response, Messages.VersionResponse.REVISION);
	}
	
	RequestSocket createRequestSocket(String endpoint, String responderIdentity) throws SocketException {
		
		RequestSocket requestSocket = new RequestSocket(contextImpl, endpoint, responderIdentity, timeout, parser);
		
		return requestSocket;
	}
	
	RequestSocket createRequestSocket(String endpoint, String responderIdentity, int timeout) throws SocketException {
		
		RequestSocket requestSocket = new RequestSocket(contextImpl, endpoint, responderIdentity, timeout, parser);
		
		return requestSocket;
	}
	
	/**
	 * Connects to the server. Returns false if there is no connection.
	 * It must be called to initialize the receiving status.
	 */
	public boolean isAvailable(int timeout) {

		boolean connected = isConnected(timeout);
		
		if (connected && eventThread == null) {
			// start the status thread
			eventThread = new EventThread(this, openEventStream());
			eventThread.start();
		}
		
		return connected;
	}
	
	public boolean isAvailable() {
		return isAvailable(10000);
	}
	
	private void terminateStatusThread() {

		// the thread must terminate after the socket receives the CANCEL message
		if (eventThread != null) {
		
			try {
				eventThread.cancel();
				eventThread.join();
			}
			catch (InterruptedException e) {
			}
		}
	}

	public void terminate() {

		terminateStatusThread();
		requestSocket.terminate();
		contextImpl.terminate();
	}
	
	public void registerEventListener(EventListener listener) {
		eventListeners.add(listener);
	}
	
	public void unregisterEventListener(EventListener listener) {
		eventListeners.remove(listener);
	}
	
	ConcurrentLinkedDeque<EventListener> getEventListeners() {
		return eventListeners;
	}

	public JSONObject requestJSON(JSONObject request) {
		return requestSocket.requestJSON(request);
	}
	
	public JSONObject requestJSON(JSONObject request, byte[] data) {
		return requestSocket.requestJSON(request, data);
	}

	private int retrieveStreamPort(String name) throws ConnectionTimeout {
		
		JSONObject request = Messages.createOutputPortRequest(name);
		JSONObject response = requestSocket.requestJSON(request);
		
		return JSON.getInt(response, Messages.RequestResponse.VALUE);
	}

	private int retrieveStatusPort() throws ConnectionTimeout {
		
		JSONObject request = Messages.createStreamStatusRequest();
		JSONObject response = requestSocket.requestJSON(request);
		
		return JSON.getInt(response, Messages.RequestResponse.VALUE);
	}
	
	private int retrievePublisherProxyPort() throws ConnectionTimeout {
		
		JSONObject request = Messages.createPublisherProxyPortRequest();
		JSONObject response = requestSocket.requestJSON(request);
		
		return JSON.getInt(response, Messages.RequestResponse.VALUE);
	}
	
	private int retrieveSubscriberProxyPort() throws ConnectionTimeout {
		
		JSONObject request = Messages.createSubscriberProxyPortRequest();
		JSONObject response = requestSocket.requestJSON(request);
		
		return JSON.getInt(response, Messages.RequestResponse.VALUE);
	}
	
	/**
	 * 
	 * @throws ConnectionTimeout 
	 */
	private EventStreamSocket openEventStream() {

		if (useProxy) {
			// With the proxy, the status port is the publisher proxy port.
			statusPort = publisherProxyPort;
		}
		else {
			statusPort = serverStatusPort;
		}

		EventStreamSocket eventStreamSocket = new EventStreamSocket();
		eventStreamSocket.init(contextImpl, serverEndpoint.withPort(statusPort), requestSocket, parser);
		
		return eventStreamSocket; 
	}
	
	/**
	 * send start request with parameters
	 * 
	 * @param name
	 * @param args
	 * @param returnResult 
	 * @return null, if reply is null, else Response
	 * @throws ConnectionTimeout 
	 */
	private Response startApplication(String name, String[] args) throws ConnectionTimeout {
		
		JSONObject request;
		
		if (This.getEndpoint() != null) {
			request = Messages.createStartRequest(name, args, This.getName(), This.getId(), This.getEndpoint().toString());
		}
		else {
			request = Messages.createStartRequest(name, args, null, 0, null);
		}
		
		JSONObject response = requestSocket.requestJSON(request);
		
		return new Response(JSON.getInt(response, Messages.RequestResponse.VALUE), JSON.getString(response, Messages.RequestResponse.MESSAGE));
	}
	
	/**
	 * Sends start request with parameters and Result object.
	 * If the outputStream argument is true, then if the application has enabled output stream, an OutputStreamSocket is created.
	 * It must be destroyed (OutputPrintThread does it) to avoid blocking in terminate().
	 * 
	 * @throws ConnectionTimeout 
	 */
	public Instance start(String name, String[] args, int options) {
		
		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
				
		Instance instance = new Instance(this);
		
		// We set the name of the application and register before starting because the id is not available.
		instance.setName(name);
		registerEventListener(instance);
		
		try {
			// Connect to the stream port. A sync is made to ensure that the subscriber is connected.
			OutputStreamSocket streamSocket = null;
			
			if (outputStream) {
				streamSocket = createOutputStreamSocket(name);
			}
			
			Response response = startApplication(name, args);
			
			if (response.getValue() == -1) {
				instance.setErrorMessage(response.getMessage());
			}
			else {
				instance.setId(response.getValue());
				
				if (outputStream) {
					instance.setOutputStreamSocket(streamSocket);
				}
			}
		}
		catch (ConnectionTimeout e) {
			instance.setErrorMessage(e.getMessage());
		}
				
		return instance;
	}
	
	public Instance start(String name, String[] args) {
		return start(name, args, 0);
	}
	
	/**
	 * Sends start request without parameters and Result object.
	 * If the outputStream argument is true, then if the application has enabled output stream, an OutputStreamSocket is created.
	 * It must be destroyed (OutputPrintThread does it) to avoid blocking in terminate().
	 * 
	 * @throws ConnectionTimeout 
	 */
	public Instance start(String name, int options) {
		return start(name, null, options);
	}
	
	public Instance start(String name) {
		return start(name, 0);
	}
		
	/**
	 * stop application asynchronous
	 * 
	 * @param id
	 * @return null, if reply is null, else Response
	 * @throws ConnectionTimeout 
	 */
	Response stopApplicationAsynchronously(int id, boolean immediately) throws ConnectionTimeout {

		JSONObject request;
		
		if (immediately) {
			request = Messages.createKillRequest(id);
		}
		else {
			request = Messages.createStopRequest(id);
		}
		
		JSONObject response = requestSocket.requestJSON(request);
		
		return new Response(id, JSON.getString(response, Messages.RequestResponse.MESSAGE));
	}
		
	public void killAllAndWaitFor(String name) {
		
		List<Instance> applications = connectAll(name, Option.NONE);
		
		for (Instance application : applications) {
			application.kill();
			application.waitFor();
		}
	}
	
	private List<Instance> getInstancesFromApplicationInfos(JSONObject response, boolean outputStream) {
		
		List<Instance> instances = new ArrayList<Instance>();
		
		try {
			// Get the list of application info.
			JSONArray list = JSON.getArray(response, Messages.ApplicationInfoListResponse.APPLICATION_INFO);
						
			for (int i = 0; i < list.size(); ++i) {
				JSONObject applicationInfo = (JSONObject)list.get(i);

				// Create a new instance.
				Instance instance = new Instance(this);
			
				// Get the name.
				String name = JSON.getString(applicationInfo, Messages.ApplicationInfo.NAME);
				
				// We set the name of the application and register before starting because the id is not available.
				instance.setName(name);
				registerEventListener(instance);
				
				int applicationId = JSON.getInt(applicationInfo, Messages.ApplicationInfo.ID);
				
				// Test if the application is still alive otherwise we could have missed a status message.
				if (isAlive(applicationId)) {
					
					instance.setId(applicationId);
					instance.setInitialState(JSON.getInt(applicationInfo, Messages.ApplicationInfo.APPLICATION_STATE));
					instance.setPastStates(JSON.getInt(applicationInfo, Messages.ApplicationInfo.PAST_APPLICATION_STATES));
					
					if (outputStream) {
						instance.setOutputStreamSocket(createOutputStreamSocket(name));
					}
					
					instances.add(instance);
				}
				else {
					// It is important not to forget to unregister the result, otherwise a memory leak will occur.
					unregisterEventListener(instance);
				}
			}
		}
		catch (ConnectionTimeout e) {
			return null;
		}
	
		return instances;
	}
	
	/**
	 * 
	 * @return List of Instance, null if a connection timeout occurs
	 * @throws ConnectionTimeout
	 */
	public List<Instance> connectAll(String name, int options) {

		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
		
		JSONObject request = Messages.createConnectRequest(name);
		JSONObject response = requestSocket.requestJSON(request);

		return getInstancesFromApplicationInfos(response, outputStream);
	}
	
	/**
	 * 
	 * @return List of Instance, null if a connection timeout occurs
	 * @throws ConnectionTimeout
	 */
	public List<Instance> connectAll(String name) {
		return connectAll(name, 0);
	}
	
	/**
	 * 
	 * @return Returns the first application with name.
	 * @throws ConnectionTimeout
	 */
	public Instance connect(String name, int options) {
		List<Instance> instances = connectAll(name, options);
		
		if (instances.size() == 0) {
			return new Instance(this);
		}
		
		return instances.get(0);
	}
	
	public Instance connect(String name) {
		return connect(name, 0);
	}
	
	
	/**
	 * 
	 * @return Returns the application with id.
	 * @throws ConnectionTimeout
	 */
	public Instance connect(int id, int options) {
		
		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
		
		JSONObject request = Messages.createConnectWithIdRequest(id);
		JSONObject response = requestSocket.requestJSON(request);
		
		List<Instance> instances = getInstancesFromApplicationInfos(response, outputStream);

		if (instances.size() == 0) {
			return new Instance(this);
		}
		
		return instances.get(0);
	}
	
	/**
	 * 
	 * @return List of ApplicationConfig if everything is ok, else null
	 * @throws ConnectionTimeout
	 */
	public List<Application.Configuration> getApplicationConfigurations() {

		JSONObject request = Messages.createListRequest();
		JSONObject response = requestSocket.requestJSON(request);
		
		LinkedList<Application.Configuration> applications = new LinkedList<Application.Configuration>();

		// Get the list of application info.
		JSONArray list = JSON.getArray(response, Messages.ApplicationConfigListResponse.APPLICATION_CONFIG);
		
		for (int i = 0; i < list.size(); ++i) {
			JSONObject config = (JSONObject)list.get(i);
			
			String name = JSON.getString(config, Messages.ApplicationConfig.NAME);
			String description = JSON.getString(config, Messages.ApplicationConfig.DESCRIPTION);
			boolean runsSingle = JSON.getBoolean(config, Messages.ApplicationConfig.RUNS_SINGLE);
			boolean restart = JSON.getBoolean(config, Messages.ApplicationConfig.RESTART);
			int startingTime = JSON.getInt(config, Messages.ApplicationConfig.STARTING_TIME);
			int stoppingTime = JSON.getInt(config, Messages.ApplicationConfig.STOPPING_TIME);
		
			applications.add(new Application.Configuration(name, description, runsSingle, restart, startingTime, stoppingTime));
		}
	
		return applications;
	}
	
	/**
	 * 
	 * @return List of ApplicationInfoForClient if everything is ok, else null
	 * @throws ConnectionTimeout 
	 */
	public List<Application.Info> getApplicationInfos() {

		JSONObject request = Messages.createAppsRequest();
		JSONObject response = requestSocket.requestJSON(request);
		
		LinkedList<Application.Info> applications = new LinkedList<Application.Info>();
		
		// Get the list of application info.
		JSONArray list = JSON.getArray(response, Messages.ApplicationInfoListResponse.APPLICATION_INFO);
		
		for (int i = 0; i < list.size(); ++i) {
			JSONObject info = (JSONObject)list.get(i);
			
			String name = JSON.getString(info, Messages.ApplicationInfo.NAME);
			int id = JSON.getInt(info, Messages.ApplicationInfo.ID);
			int pid = JSON.getInt(info, Messages.ApplicationInfo.PID);
			int state = JSON.getInt(info, Messages.ApplicationInfo.APPLICATION_STATE);
			int pastStates = JSON.getInt(info, Messages.ApplicationInfo.PAST_APPLICATION_STATES);
			String args = JSON.getString(info, Messages.ApplicationInfo.ARGS);
		
			applications.add(new Application.Info(name, id, pid, state, pastStates, args));
		}
	
		return applications;
	}
	
	/**
	 * 
	 * @param name
	 * @return the of application info of the applications with name
	 * @throws ConnectionTimeout 
	 */
	public List<Application.Info> getApplicationInfos(String name) {
		
		List<Application.Info> applicationInfos = getApplicationInfos();
		List<Application.Info> result = new LinkedList<Application.Info>();
		
		for (Application.Info i : applicationInfos) {
			if (i.getName().equals(name)) {
				result.add(i);
			}
		}
		
		return result;
	}
	

	public int getActualState(int id) {
		
		JSONObject response = requestSocket.requestJSON(Messages.createGetStatusRequest(id));
		
		return JSON.getInt(response, Messages.StatusEvent.APPLICATION_STATE);
	}

	public Set<Integer> getPastStates(int id) {
		
		JSONObject response = requestSocket.requestJSON(Messages.createGetStatusRequest(id));
		
		int applicationStates = JSON.getInt(response, Messages.StatusEvent.PAST_APPLICATION_STATES);
		
		Set<Integer> result = new HashSet<Integer>();
		
		if ((applicationStates & State.STARTING) != 0) {
			result.add(State.STARTING);
		}
		
		if ((applicationStates & State.RUNNING) != 0) {
			result.add(State.RUNNING);
		}
		
		if ((applicationStates & State.STOPPING) != 0) {
			result.add(State.STOPPING);
		}
		
		if ((applicationStates & State.KILLING) != 0) {
			result.add(State.KILLING);
		}
		
		if ((applicationStates & State.PROCESSING_ERROR) != 0) {
			result.add(State.PROCESSING_ERROR);
		}
		
		if ((applicationStates & State.ERROR) != 0) {
			result.add(State.ERROR);
		}
		
		if ((applicationStates & State.SUCCESS) != 0) {
			result.add(State.SUCCESS);
		}
		
		if ((applicationStates & State.STOPPED) != 0) {
			result.add(State.STOPPED);
		}
		
		if ((applicationStates & State.KILLED) != 0) {
			result.add(State.KILLED);
		}
		
		return result;
	}

	private OutputStreamSocket createOutputStreamSocket(String name) {
		
		OutputStreamSocket outputStreamSocket = new OutputStreamSocket(name);
		
		// Even with the proxy, it is necessary to check if the application has an output stream.
		int port = retrieveStreamPort(name);
		if (port == -1) {
			return null;
		}
		
		// If use proxy, stream port is the port of the publisher proxy which is the same port as status port.
		if (useProxy) {
			port = statusPort;
		}
		
		outputStreamSocket.init(contextImpl, serverEndpoint.withPort(port), requestSocket, parser);
		
		return outputStreamSocket; 
	}
	
	/**
	 * send request to ask if an application is alive
	 * 
	 * @param id
	 * @return boolean
	 * @throws ConnectionTimeout 
	 */
	private boolean isAlive(int id) {

		JSONObject request = Messages.createIsAliveRequest(id);
		JSONObject response = requestSocket.requestJSON(request);
		
		return JSON.getBoolean(response, Messages.IsAliveResponse.IS_ALIVE);
	}
	
	/**
	 * send parameters to an application
	 * 
	 * @param id
	 * @param inputs
	 * @return null, if reply is null, else Response
	 * @throws WriteException 
	 * @throws ConnectionTimeout 
	 */
	public void writeToInputStream(int id, String[] inputs) throws WriteException {

		JSONObject request = Messages.createWriteInputRequest(id, inputs);
		JSONObject response = requestSocket.requestJSON(request);
		
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		
		if (value == -1) {
			throw new WriteException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
	}
	
	/**
	 * send parameters to an application
	 * 
	 * @param id
	 * @param parametersArray
	 * @return null, if reply is null, else Response
	 * @throws WriteException 
	 * @throws ConnectionTimeout 
	 */
	public void writeToInputStream(int id, String input) throws WriteException {
		
		String[] inputArray = new String[1];
		inputArray[0] = input;
		
		writeToInputStream(id, inputArray);
	}
	
	/**
	 * 
	 * @param applicationId
	 * @param key
	 * @param value
	 * @throws UndefinedApplicationException 
	 * @throws UndefinedKeyException 
	 */
	void storeKeyValue(int applicationId, String key, String value) throws UndefinedApplicationException, KeyAlreadyExistsException {
		
		JSONObject request = Messages.createStoreKeyValueRequest(applicationId, key, value);
		JSONObject response = requestSocket.requestJSON(request);
		
		int responseValue = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (responseValue == -1) {
			throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		else if (responseValue == -2) {
			throw new KeyAlreadyExistsException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
	}
	
	/**
	 * 
	 * @param applicationId
	 * @param key
	 * @return
	 * @throws UndefinedApplicationException
	 * @throws UndefinedKeyException
	 */
	String getKeyValue(int applicationId, String key) throws UndefinedApplicationException, UndefinedKeyException {
		
		JSONObject request = Messages.createGetKeyValueRequest(applicationId, key);
		JSONObject response = requestSocket.requestJSON(request);
		
		int responseValue = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (responseValue == 0) {
			return JSON.getString(response, Messages.RequestResponse.MESSAGE);
		}
		else if (responseValue == -1) {
			throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		else if (responseValue == -2) {
			throw new UndefinedKeyException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param applicationId
	 * @param key
	 * @throws UndefinedApplicationException
	 * @throws UndefinedKeyException
	 */
	void removeKey(int applicationId, String key) throws UndefinedApplicationException, UndefinedKeyException {
		
		JSONObject request = Messages.createRemoveKeyRequest(applicationId, key);
		JSONObject response = requestSocket.requestJSON(request);
		
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		else if (value == -2) {
			throw new UndefinedKeyException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
	}
	
	public int requestPort(int applicationId) throws UndefinedApplicationException {
		
		JSONObject request = Messages.createRequestPortRequest(applicationId);
		JSONObject response = requestSocket.requestJSON(request);
		
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		return value;
	}
	
	public void setPortUnavailable(int applicationId, int port) throws UndefinedApplicationException {
		
		JSONObject request = Messages.createPortUnavailableRequest(applicationId, port);
		JSONObject response = requestSocket.requestJSON(request);
			
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
	}
	
	public void releasePort(int applicationId, int port) throws UndefinedApplicationException {
		
		JSONObject request = Messages.createReleasePortRequest(applicationId, port);
		JSONObject response = requestSocket.requestJSON(request);
			
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
	}
	
	public List<Application.Port> getPorts() {
		
		JSONObject request = Messages.createPortsRequest();
		JSONObject response = requestSocket.requestJSON(request);
		
		LinkedList<Application.Port> ports = new LinkedList<Application.Port>();
		
		// Get the list of application info.
		JSONArray list = JSON.getArray(response, Messages.PortInfoListResponse.PORT_INFO);
		
		for (int i = 0; i < list.size(); ++i) {
			JSONObject info = (JSONObject)list.get(i);
							
			int port = JSON.getInt(info, Messages.PortInfo.PORT);
			String status = JSON.getString(info, Messages.PortInfo.STATUS);
			String owner = JSON.getString(info, Messages.PortInfo.OWNER);
		
			ports.add(new Application.Port(port, status, owner));
		}
		
		return ports;
	}
	
	/**
	 * Creates a connection checker.
	 * @param handler
	 * @return
	 */
	public ConnectionChecker createConnectionChecker(ConnectionChecker.Handler handler) {
		
		ConnectionChecker connectionChecker = new ConnectionChecker(this, handler);
		connectionChecker.start(getAvailableTimeout(), 10000);
		
		return connectionChecker;
	}
	
	/**
	 * Creates a connection checker.
	 * @param handler
	 * @param pollingTimeMs
	 * @return
	 */
	public ConnectionChecker createConnectionChecker(ConnectionChecker.Handler handler, int pollingTimeMs) {
		
		ConnectionChecker connectionChecker = new ConnectionChecker(this, handler);
		connectionChecker.start(getAvailableTimeout(), pollingTimeMs);
		
		return connectionChecker;
	}

	@Override
	public String toString() {
		return "server@" + serverEndpoint;
	}

}