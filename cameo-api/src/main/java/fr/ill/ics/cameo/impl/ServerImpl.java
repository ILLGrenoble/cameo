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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.EventListener;
import fr.ill.ics.cameo.EventStreamSocket;
import fr.ill.ics.cameo.InvalidArgumentException;
import fr.ill.ics.cameo.Option;
import fr.ill.ics.cameo.OutputStreamException;
import fr.ill.ics.cameo.OutputStreamSocket;
import fr.ill.ics.cameo.SubscriberCreationException;
import fr.ill.ics.cameo.UndefinedApplicationException;
import fr.ill.ics.cameo.UndefinedKeyException;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.WriteException;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Msg;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;

/**
 * The server class is thread-safe except for the connect and terminate methods that must be called respectively 
 * before and after any concurrent calls.
 * @author legoc
 *
 */
public class ServerImpl extends ServicesImpl {

	private ConcurrentLinkedDeque<EventListener> eventListeners = new ConcurrentLinkedDeque<EventListener>(); 
	private EventThread eventThread;
		
	/**
	 * Constructor with endpoint.
	 * This constructor must be used when the services are related to another cameo server that
	 * has not started the current application.
	 * Some methods may throw the runtime ConnectionTimeout exception, so it is recommended to catch the exception at a global scope if a timeout is set. 
	 * @param endpoint
	 */
	public ServerImpl(String endpoint, int timeout) {
		
		this.timeout = timeout;
		
		String[] tokens = endpoint.split(":");

		// check length
		if (tokens.length < 3) {
			throw new InvalidArgumentException(endpoint + " is not a valid endpoint");
		}
		
		url = tokens[0] + ":" + tokens[1];
		port = Integer.parseInt(tokens[2]);
		serverEndpoint = url + ":" + port;
		
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
	 * Connects to the server. Returns false if there is no connection.
	 * It must be called to initialize the receiving status.
	 */
	public boolean isAvailable(int timeout) {

		boolean connected = super.isAvailable(timeout);
		
		if (connected && eventThread == null) {
			// start the status thread
			eventThread = new EventThread(this, openEventStream());
			eventThread.start();
		}
		
		return connected;
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
		super.terminate();
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
	
	/**
	 * send start request with parameters
	 * 
	 * @param name
	 * @param args
	 * @param instanceReference 
	 * @param returnResult 
	 * @return null, if reply is null, else Response
	 * @throws ConnectionTimeout 
	 */
	private fr.ill.ics.cameo.impl.Response startApplication(String name, String[] args, String instanceReference) throws ConnectionTimeout {
		
		Zmq.Msg request = createStartRequest(name, args, instanceReference);
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return new fr.ill.ics.cameo.impl.Response(JSON.getInt(response, Message.RequestResponse.VALUE), JSON.getString(response, Message.RequestResponse.MESSAGE));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	private int getStreamPort(String name) throws ConnectionTimeout {
		
		Zmq.Msg request = createOutputPortRequest(name);
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return JSON.getInt(response, Message.RequestResponse.VALUE);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	/**
	 * Sends start request with parameters and Result object.
	 * If the outputStream argument is true, then if the application has enabled output stream, an OutputStreamSocket is created.
	 * It must be destroyed (OutputPrintThread does it) to avoid blocking in terminate().
	 * 
	 * @throws ConnectionTimeout 
	 */
	public InstanceImpl start(String name, String[] args, int options, String instanceReference) {
		
		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
				
		InstanceImpl instance = new InstanceImpl(this);
		
		// We set the name of the application and register before starting because the id is not available.
		instance.setName(name);
		registerEventListener(instance);
		
		try {
			// We connect to the stream port before starting the application
			// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly.
			if (outputStream) {
				instance.setOutputStreamSocket(createOutputStreamSocket(getStreamPort(name)));
			}
			
			Response response = startApplication(name, args, instanceReference);
			
			if (response.getValue() == -1) {
				instance.setErrorMessage(response.getMessage());
			}
			else {
				instance.setId(response.getValue());
			}
		}
		catch (ConnectionTimeout e) {
			instance.setErrorMessage(e.getMessage());
		}
				
		return instance;
	}
	
	public InstanceImpl start(String name, String[] args, String serverEndpoint) {
		return start(name, args, 0, serverEndpoint);
	}
	
	/**
	 * Sends start request without parameters and Result object.
	 * If the outputStream argument is true, then if the application has enabled output stream, an OutputStreamSocket is created.
	 * It must be destroyed (OutputPrintThread does it) to avoid blocking in terminate().
	 * 
	 * @throws ConnectionTimeout 
	 */
	public InstanceImpl start(String name, int options, String instanceReference) {
		return start(name, null, options, instanceReference);
	}
	
	public InstanceImpl start(String name, String serverEndpoint) {
		return start(name, 0, serverEndpoint);
	}
		
	/**
	 * stop application asynchronous
	 * 
	 * @param id
	 * @return null, if reply is null, else Response
	 * @throws ConnectionTimeout 
	 */
	fr.ill.ics.cameo.impl.Response stopApplicationAsynchronously(int id, boolean immediately) throws ConnectionTimeout {

		Zmq.Msg request;
		
		if (immediately) {
			request = createKillRequest(id);
		}
		else {
			request = createStopRequest(id);
		}
		
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return new fr.ill.ics.cameo.impl.Response(id, JSON.getString(response, Message.RequestResponse.MESSAGE));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
		
	public void killAllAndWaitFor(String name) {
		
		List<InstanceImpl> applications = connectAll(name);
		
		for (InstanceImpl application : applications) {
			application.kill();
			application.waitFor();
		}
	}
	
	/**
	 * 
	 * @return List of Instance, null if a connection timeout occurs
	 * @throws ConnectionTimeout
	 */
	public List<InstanceImpl> connectAll(String name, int options) {

		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
		
		List<InstanceImpl> instances = new ArrayList<InstanceImpl>();
		
		Zmq.Msg request = createConnectRequest(name);
		Zmq.Msg reply = requestSocket.request(request);

		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);

			// Get the list of application info.
			JSONArray list = JSON.getArray(response, Message.ApplicationInfoListResponse.APPLICATION_INFO);
			
			for (int i = 0; i < list.size(); ++i) {
				JSONObject applicationInfo = (JSONObject)list.get(i);

				// Create a new instance.
				InstanceImpl instance = new InstanceImpl(this);
				
				// We set the name of the application and register before starting because the id is not available.
				instance.setName(name);
				registerEventListener(instance);
				
				int applicationId = JSON.getInt(applicationInfo, Message.ApplicationInfo.ID);
				
				// Test if the application is still alive otherwise we could have missed a status message.
				if (isAlive(applicationId)) {
					// We connect to the stream port before starting the application
					// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly.
					if (outputStream) {
						instance.setOutputStreamSocket(createOutputStreamSocket(getStreamPort(name)));
					}
										
					instance.setId(applicationId);
					instance.setInitialState(JSON.getInt(applicationInfo, Message.ApplicationInfo.APPLICATION_STATE));
					instance.setInitialState(JSON.getInt(applicationInfo, Message.ApplicationInfo.PAST_APPLICATION_STATES));
					
					instances.add(instance);
				}
				else {
					// It is important not to forget to unregister the result, otherwise a memory leak will occur.
					unregisterEventListener(instance);
				}
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
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
	public List<InstanceImpl> connectAll(String name) {
		return connectAll(name, Option.NONE);
	}
	
	/**
	 * 
	 * @return Returns the first application with name.
	 * @throws ConnectionTimeout
	 */
	public InstanceImpl connect(String name, int options) {
		List<InstanceImpl> instances = connectAll(name, options);
		
		if (instances.size() == 0) {
			return new InstanceImpl(this);
		}
		
		return instances.get(0);
	}
	
	/**
	 * 
	 * @return List of ApplicationConfig if everything is ok, else null
	 * @throws ConnectionTimeout
	 */
	public List<Application.Configuration> getApplicationConfigurations() {

		Zmq.Msg request = createListRequest();
		Zmq.Msg reply = requestSocket.request(request);
		
		LinkedList<Application.Configuration> applications = new LinkedList<Application.Configuration>();

		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
						
			// Get the list of application info.
			JSONArray list = JSON.getArray(response, Message.ListResponse.APPLICATION_CONFIG);
			
			for (int i = 0; i < list.size(); ++i) {
				JSONObject config = (JSONObject)list.get(i);
				
				String name = JSON.getString(config, Message.ApplicationConfig.NAME);
				String description = JSON.getString(config, Message.ApplicationConfig.DESCRIPTION);
				boolean runsSingle = JSON.getBoolean(config, Message.ApplicationConfig.RUNS_SINGLE);
				boolean restart = JSON.getBoolean(config, Message.ApplicationConfig.RESTART);
				int startingTime = JSON.getInt(config, Message.ApplicationConfig.STARTING_TIME);
				int stoppingTime = JSON.getInt(config, Message.ApplicationConfig.STOPPING_TIME);
			
				applications.add(new Application.Configuration(name, description, runsSingle, restart, startingTime, stoppingTime));
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	
		return applications;
	}
	
	/**
	 * 
	 * @return List of ApplicationInfoForClient if everything is ok, else null
	 * @throws ConnectionTimeout 
	 */
	public List<Application.Info> getApplicationInfos() {

		Zmq.Msg request = createAppsRequest();
		Zmq.Msg reply = requestSocket.request(request);
		
		LinkedList<Application.Info> applications = new LinkedList<Application.Info>();
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
						
			// Get the list of application info.
			JSONArray list = JSON.getArray(response, Message.ApplicationInfoListResponse.APPLICATION_INFO);
			
			for (int i = 0; i < list.size(); ++i) {
				JSONObject info = (JSONObject)list.get(i);
				
				String name = JSON.getString(info, Message.ApplicationInfo.NAME);
				int id = JSON.getInt(info, Message.ApplicationInfo.ID);
				int pid = JSON.getInt(info, Message.ApplicationInfo.PID);
				int state = JSON.getInt(info, Message.ApplicationInfo.APPLICATION_STATE);
				int pastStates = JSON.getInt(info, Message.ApplicationInfo.PAST_APPLICATION_STATES);
				String args = JSON.getString(info, Message.ApplicationInfo.ARGS);
			
				applications.add(new Application.Info(name, id, pid, state, pastStates, args));
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
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

	private OutputStreamSocket createOutputStreamSocket(int port) {
		
		if (port == -1) {
			return null;
		}
		
		// Prepare our context and subscriber
		Zmq.Socket subscriber = context.createSocket(Zmq.SUB);
		
		subscriber.connect(url + ":" + port);
		subscriber.subscribe(Message.Event.STREAM);
		subscriber.subscribe(Message.Event.ENDSTREAM);
		
		String cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Message.Event.CANCEL);
		
		Zmq.Socket cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);
		
		return new OutputStreamSocket(this, subscriber, cancelPublisher);
	}
		
	/**
	 * Returns a OutputStreamSocket if the application with id exists, else null.
	 * The application may have existed.
	 * 
	 * @param id
	 * @return
	 * @throws OutputStreamException
	 * @throws ConnectionTimeout 
	 */
	public OutputStreamSocket openOutputStream(int id) throws OutputStreamException {

		Zmq.Msg request = createOutputPortWithIdRequest(id);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		int port = JSON.getInt(response, Message.RequestResponse.VALUE);
		
		// In case of error, the returned value is -1.
		if (port == -1) {
			throw new OutputStreamException(JSON.getString(response, Message.RequestResponse.MESSAGE));
		}
		
		return createOutputStreamSocket(port);
	}
	
	/**
	 * send request to ask if an application is alive
	 * 
	 * @param id
	 * @return boolean
	 * @throws ConnectionTimeout 
	 */
	private boolean isAlive(int id) {

		Zmq.Msg request = createIsAliveRequest(id);
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return JSON.getBoolean(response, Message.IsAliveResponse.IS_ALIVE);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
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

		Zmq.Msg request = createWriteInputRequest(id, inputs);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		int value = JSON.getInt(response, Message.RequestResponse.VALUE);
		
		if (value == -1) {
			throw new WriteException(JSON.getString(response, Message.RequestResponse.MESSAGE));
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
	 * @param applicationName
	 * @param publisherName
	 * @return
	 * @throws SubscriberCreationException
	 * @throws ConnectionTimeout
	 */
	public SubscriberImpl createSubscriber(int applicationId, String publisherName, InstanceImpl instance) throws SubscriberCreationException {
		
		Zmq.Msg request = createConnectPublisherRequest(applicationId, publisherName);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		int publisherPort = JSON.getInt(response, Message.PublisherResponse.PUBLISHER_PORT);
		
		if (publisherPort == -1) {
			throw new SubscriberCreationException(JSON.getString(response, Message.RequestResponse.MESSAGE));
		}
		
		int synchronizerPort = JSON.getInt(response, Message.PublisherResponse.SYNCHRONIZER_PORT);
		int numberOfSubscribers = JSON.getInt(response, Message.PublisherResponse.NUMBER_OF_SUBSCRIBERS);
		
		SubscriberImpl subscriber = new SubscriberImpl(this, context, url, publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instance);
		subscriber.init();
		
		return subscriber;
	}
	
	/**
	 * 
	 * @param applicationId
	 * @param key
	 * @param value
	 */
	public void storeKeyValue(int applicationId, String key, String value) {
		
		Zmq.Msg request = createStoreKeyValueRequest(applicationId, key, value);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
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
	public String getKeyValue(int applicationId, String key) throws UndefinedApplicationException, UndefinedKeyException {
		
		Zmq.Msg request = createGetKeyValueRequest(applicationId, key);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
			
			int value = JSON.getInt(response, Message.RequestResponse.VALUE);
			if (value == 0) {
				return JSON.getString(response, Message.RequestResponse.MESSAGE);
			}
			else if (value == -1) {
				throw new UndefinedApplicationException(JSON.getString(response, Message.RequestResponse.MESSAGE));
			}
			else if (value == -2) {
				throw new UndefinedKeyException(JSON.getString(response, Message.RequestResponse.MESSAGE));
			}
			
			return null;
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	/**
	 * 
	 * @param applicationId
	 * @param key
	 * @throws UndefinedApplicationException
	 * @throws UndefinedKeyException
	 */
	public void removeKey(int applicationId, String key) throws UndefinedApplicationException, UndefinedKeyException {
		
		Zmq.Msg request = createRemoveKeyRequest(applicationId, key);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
			
			int value = JSON.getInt(response, Message.RequestResponse.VALUE);
			if (value == -1) {
				throw new UndefinedApplicationException(JSON.getString(response, Message.RequestResponse.MESSAGE));
			}
			else if (value == -2) {
				throw new UndefinedKeyException(JSON.getString(response, Message.RequestResponse.MESSAGE));
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	/**
	 * create isAlive request
	 * 
	 * @param text
	 * @return
	 */
	private Zmq.Msg createIsAliveRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.IS_ALIVE);
		request.put(Message.IsAliveRequest.ID, id);

		return message(request);
	}
	
	/**
	 * create start request with parameters
	 * 
	 * @param name
	 * @param args
	 * @param instanceReference 
	 * @param returnResult 
	 * @return request
	 */
	private Zmq.Msg createStartRequest(String name, String[] args, String instanceReference) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.START);
		request.put(Message.StartRequest.NAME, name);
		request.put(Message.StartRequest.INSTANCE_REFERENCE, instanceReference);
		
		if (args != null) {
			JSONArray list = new JSONArray();
			for (int i = 0; i < args.length; i++) {
				list.add(args[i]);
			}
			request.put(Message.StartRequest.ARGS, list);
		}

		return message(request);
	}

	/**
	 * create stop request
	 * 
	 * @param id
	 * @return request
	 */
	private Zmq.Msg createStopRequest(int id) {

		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.STOP);
		request.put(Message.StopRequest.ID, id);

		return message(request);
	}
	
	/**
	 * create kill request
	 * 
	 * @param id
	 * @return request
	 */
	private Zmq.Msg createKillRequest(int id) {

		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.KILL);
		request.put(Message.KillRequest.ID, id);

		return message(request);
	}

	/**
	 * create connect request
	 * 
	 * @param name
	 * @param argsOfApplication
	 * @return request
	 */
	private Zmq.Msg createConnectRequest(String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.CONNECT);
		request.put(Message.ConnectRequest.NAME, name);

		return message(request);
	}
	
	/**
	 * create all available request
	 * 
	 * @return request
	 */
	private Zmq.Msg createListRequest() {

		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.LIST);

		return message(request);
	}
	
	/**
	 * create showall request
	 * 
	 * @return request
	 */
	private Zmq.Msg createAppsRequest() {

		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.APPS);

		return message(request);
	}

	/**
	 * create showall request
	 * 
	 * @return request
	 */
	private Zmq.Msg createOutputPortWithIdRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.OUTPUT_PORT_WITH_ID);
		request.put(Message.OutputPortWithIdRequest.ID, id);

		return message(request);
	}
	
	/**
	 * create WriteInput request
	 * 
	 * @param id
	 * @param inputs
	 * @return
	 */
	private Zmq.Msg createWriteInputRequest(int id, String[] inputs) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.WRITE_INPUT);
		request.put(Message.WriteInputRequest.ID, id);
		
		JSONArray list = new JSONArray();
		for (int i = 0; i < inputs.length; i++) {
			list.add(inputs[i]);
		}
		request.put(Message.WriteInputRequest.PARAMETERS, list);

		return message(request);
	}
	
	/**
	 * create output request
	 * 
	 * @param name
	 */
	private Zmq.Msg createOutputPortRequest(String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.OUTPUT_PORT);
		request.put(Message.OutputRequest.NAME, name);

		return message(request);
	}
	
	private Zmq.Msg createConnectPublisherRequest(int applicationId, String publisherName) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.CONNECT_PUBLISHER);
		request.put(Message.ConnectPublisherRequest.APPLICATION_ID, applicationId);
		request.put(Message.ConnectPublisherRequest.PUBLISHER_NAME, publisherName);

		return message(request);
	}
	
	protected Zmq.Msg createSubscribePublisherRequest() {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.SUBSCRIBE_PUBLISHER);

		return message(request);
	}

	private Msg createStoreKeyValueRequest(int applicationId, String key, String value) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.STORE_KEY_VALUE);
		request.put(Message.StoreKeyValueRequest.ID, applicationId);
		request.put(Message.StoreKeyValueRequest.KEY, key);
		request.put(Message.StoreKeyValueRequest.VALUE, value);

		return message(request);
	}

	private Msg createGetKeyValueRequest(int applicationId, String key) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.GET_KEY_VALUE);
		request.put(Message.GetKeyValueRequest.ID, applicationId);
		request.put(Message.GetKeyValueRequest.KEY, key);

		return message(request);
	}
	
	private Msg createRemoveKeyRequest(int applicationId, String key) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.REMOVE_KEY);
		request.put(Message.RemoveKeyRequest.ID, applicationId);
		request.put(Message.RemoveKeyRequest.KEY, key);

		return message(request);
	}
	
	@Override
	public String toString() {
		return "server@" + serverEndpoint;
	}
}