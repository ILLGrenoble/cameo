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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.Application.State;
import fr.ill.ics.cameo.base.Application.This;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.EventListener;
import fr.ill.ics.cameo.base.EventStreamSocket;
import fr.ill.ics.cameo.base.InvalidArgumentException;
import fr.ill.ics.cameo.base.Option;
import fr.ill.ics.cameo.base.OutputStreamSocket;
import fr.ill.ics.cameo.base.UndefinedApplicationException;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.base.UnexpectedException;
import fr.ill.ics.cameo.base.WriteException;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

/**
 * The server class is thread-safe except for the connect and terminate methods that must be called respectively 
 * before and after any concurrent calls.
 * @author legoc
 *
 */
public class ServerImpl extends ServicesImpl {

	private ConcurrentLinkedDeque<EventListener> eventListeners = new ConcurrentLinkedDeque<EventListener>(); 
	private EventThread eventThread;
		
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
	 * Constructor with endpoint.
	 * This constructor must be used when the services are related to another cameo server that
	 * has not started the current application.
	 * Some methods may throw the runtime ConnectionTimeout exception, so it is recommended to catch the exception at a global scope if a timeout is set. 
	 * @param endpoint
	 */
	public ServerImpl(Endpoint endpoint, int timeout) {
		this.initServer(endpoint, timeout);
	}
	
	public ServerImpl(String endpoint, int timeout) {

		try {
			this.initServer(Endpoint.parse(endpoint), timeout);
		}
		catch (Exception e) {
			throw new InvalidArgumentException(endpoint + " is not a valid endpoint");
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
	 * @param returnResult 
	 * @return null, if reply is null, else Response
	 * @throws ConnectionTimeout 
	 */
	private fr.ill.ics.cameo.base.impl.Response startApplication(String name, String[] args) throws ConnectionTimeout {
		
		JSONObject request;
		
		if (This.getEndpoint() != null) {
			request = Messages.createStartRequest(name, args, This.getName(), This.getId(), This.getEndpoint().toString());
		}
		else {
			request = Messages.createStartRequest(name, args, null, 0, null);
		}
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return new fr.ill.ics.cameo.base.impl.Response(JSON.getInt(response, Messages.RequestResponse.VALUE), JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	private int getStreamPort(String name) throws ConnectionTimeout {
		
		JSONObject request = Messages.createOutputPortRequest(name);
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return JSON.getInt(response, Messages.RequestResponse.VALUE);
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
	public InstanceImpl start(String name, String[] args, int options) {
		
		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
				
		InstanceImpl instance = new InstanceImpl(this);
		
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
	
	public InstanceImpl start(String name, String[] args) {
		return start(name, args, 0);
	}
	
	/**
	 * Sends start request without parameters and Result object.
	 * If the outputStream argument is true, then if the application has enabled output stream, an OutputStreamSocket is created.
	 * It must be destroyed (OutputPrintThread does it) to avoid blocking in terminate().
	 * 
	 * @throws ConnectionTimeout 
	 */
	public InstanceImpl start(String name, int options) {
		return start(name, null, options);
	}
	
	public InstanceImpl start(String name) {
		return start(name, 0);
	}
		
	/**
	 * stop application asynchronous
	 * 
	 * @param id
	 * @return null, if reply is null, else Response
	 * @throws ConnectionTimeout 
	 */
	fr.ill.ics.cameo.base.impl.Response stopApplicationAsynchronously(int id, boolean immediately) throws ConnectionTimeout {

		JSONObject request;
		
		if (immediately) {
			request = Messages.createKillRequest(id);
		}
		else {
			request = Messages.createStopRequest(id);
		}
		
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return new fr.ill.ics.cameo.base.impl.Response(id, JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
		
	public void killAllAndWaitFor(String name) {
		
		List<InstanceImpl> applications = connectAll(name, Option.NONE);
		
		for (InstanceImpl application : applications) {
			application.kill();
			application.waitFor();
		}
	}
	
	private List<InstanceImpl> getInstancesFromApplicationInfos(Zmq.Msg reply, boolean outputStream) {
		
		List<InstanceImpl> instances = new ArrayList<InstanceImpl>();
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			// Get the list of application info.
			JSONArray list = JSON.getArray(response, Messages.ApplicationInfoListResponse.APPLICATION_INFO);
						
			for (int i = 0; i < list.size(); ++i) {
				JSONObject applicationInfo = (JSONObject)list.get(i);

				// Create a new instance.
				InstanceImpl instance = new InstanceImpl(this);
			
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
	public List<InstanceImpl> connectAll(String name, int options) {

		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
		
		JSONObject request = Messages.createConnectRequest(name);
		Zmq.Msg reply = requestSocket.request(request);

		return getInstancesFromApplicationInfos(reply, outputStream);
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
	 * @return Returns the application with id.
	 * @throws ConnectionTimeout
	 */
	public InstanceImpl connect(int id, int options) {
		
		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
		
		JSONObject request = Messages.createConnectWithIdRequest(id);
		Zmq.Msg reply = requestSocket.request(request);
		
		List<InstanceImpl> instances = getInstancesFromApplicationInfos(reply, outputStream);

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

		JSONObject request = Messages.createListRequest();
		Zmq.Msg reply = requestSocket.request(request);
		
		LinkedList<Application.Configuration> applications = new LinkedList<Application.Configuration>();

		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
						
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

		JSONObject request = Messages.createAppsRequest();
		Zmq.Msg reply = requestSocket.request(request);
		
		LinkedList<Application.Info> applications = new LinkedList<Application.Info>();
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
						
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
	

	public int getActualState(int id) {
		
		try {
			Zmq.Msg reply = requestSocket.request(Messages.createGetStatusRequest(id));
			
			byte[] messageData = reply.getFirstData();
			
			if (messageData == null) {
				return State.UNKNOWN;
			}
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
			return JSON.getInt(response, Messages.StatusEvent.APPLICATION_STATE);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	public Set<Integer> getPastStates(int id) {
		
		try {
			Zmq.Msg reply = requestSocket.request(Messages.createGetStatusRequest(id));
			
			byte[] messageData = reply.getFirstData();
			
			if (messageData == null) {
				return null;
			}
			
			// Get the JSON response object.
			JSONObject response = parse(reply);
			
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
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	private OutputStreamSocket createOutputStreamSocket(String name) {
		
		int port = getStreamPort(name);
		
		if (port == -1) {
			return null;
		}
		
		// Prepare our context and subscriber
		Zmq.Socket subscriber = context.createSocket(Zmq.SUB);
		
		subscriber.connect(serverEndpoint.withPort(port).toString());
		subscriber.subscribe(Messages.Event.SYNCSTREAM);
		subscriber.subscribe(Messages.Event.STREAM);
		subscriber.subscribe(Messages.Event.ENDSTREAM);
		
		String cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Messages.Event.CANCEL);
		
		Zmq.Socket cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);
		
		// Polling to wait for connection.
		Zmq.Poller poller = context.createPoller(subscriber);
		
		while (true) {
			
			// the server returns a SYNCSTREAM message that is used to synchronize the subscriber
			sendSyncStream(name);

			// return at the first response.
			if (poller.poll(100)) {
				break;
			}
		}
		
		return new OutputStreamSocket(this, subscriber, cancelPublisher);
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
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			return JSON.getBoolean(response, Messages.IsAliveResponse.IS_ALIVE);
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

		JSONObject request = Messages.createWriteInputRequest(id, inputs);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
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
	 */
	public void storeKeyValue(int applicationId, String key, String value) {
		
		JSONObject request = Messages.createStoreKeyValueRequest(applicationId, key, value);
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
		
		JSONObject request = Messages.createGetKeyValueRequest(applicationId, key);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
			
			int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
			if (value == 0) {
				return JSON.getString(response, Messages.RequestResponse.MESSAGE);
			}
			else if (value == -1) {
				throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
			}
			else if (value == -2) {
				throw new UndefinedKeyException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
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
		
		JSONObject request = Messages.createRemoveKeyRequest(applicationId, key);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
			
			int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
			if (value == -1) {
				throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
			}
			else if (value == -2) {
				throw new UndefinedKeyException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public int requestPort(int applicationId) throws UndefinedApplicationException {
		
		JSONObject request = Messages.createRequestPortRequest(applicationId);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
			
			int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
			if (value == -1) {
				throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
			}
			return value;
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public void setPortUnavailable(int applicationId, int port) throws UndefinedApplicationException {
		
		JSONObject request = Messages.createPortUnavailableRequest(applicationId, port);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
			
			int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
			if (value == -1) {
				throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public void releasePort(int applicationId, int port) throws UndefinedApplicationException {
		
		JSONObject request = Messages.createReleasePortRequest(applicationId, port);
		Zmq.Msg reply = requestSocket.request(request);
		
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
			
			int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
			if (value == -1) {
				throw new UndefinedApplicationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public List<Application.Port> getPorts() {
		
		JSONObject request = Messages.createPortsRequest();
		Zmq.Msg reply = requestSocket.request(request);
		
		LinkedList<Application.Port> ports = new LinkedList<Application.Port>();
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
						
			// Get the list of application info.
			JSONArray list = JSON.getArray(response, Messages.PortInfoListResponse.PORT_INFO);
			
			for (int i = 0; i < list.size(); ++i) {
				JSONObject info = (JSONObject)list.get(i);
								
				int port = JSON.getInt(info, Messages.PortInfo.PORT);
				String status = JSON.getString(info, Messages.PortInfo.STATUS);
				String owner = JSON.getString(info, Messages.PortInfo.OWNER);
			
				ports.add(new Application.Port(port, status, owner));
			}
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		
		return ports;
	}
	
	@Override
	public String toString() {
		return "server@" + serverEndpoint;
	}
}