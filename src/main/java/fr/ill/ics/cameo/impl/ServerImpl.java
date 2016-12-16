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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.EventListener;
import fr.ill.ics.cameo.EventStreamSocket;
import fr.ill.ics.cameo.InvalidArgumentException;
import fr.ill.ics.cameo.Option;
import fr.ill.ics.cameo.OutputStreamException;
import fr.ill.ics.cameo.OutputStreamSocket;
import fr.ill.ics.cameo.SubscriberCreationException;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.WriteException;
import fr.ill.ics.cameo.proto.Messages;
import fr.ill.ics.cameo.proto.Messages.AllAvailableCommand;
import fr.ill.ics.cameo.proto.Messages.AllAvailableResponse;
import fr.ill.ics.cameo.proto.Messages.ApplicationInfoListResponse;
import fr.ill.ics.cameo.proto.Messages.ConnectCommand;
import fr.ill.ics.cameo.proto.Messages.ConnectPublisherCommand;
import fr.ill.ics.cameo.proto.Messages.IsAliveCommand;
import fr.ill.ics.cameo.proto.Messages.IsAliveResponse;
import fr.ill.ics.cameo.proto.Messages.KillCommand;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;
import fr.ill.ics.cameo.proto.Messages.OutputCommand;
import fr.ill.ics.cameo.proto.Messages.PublisherResponse;
import fr.ill.ics.cameo.proto.Messages.RequestResponse;
import fr.ill.ics.cameo.proto.Messages.SendParametersCommand;
import fr.ill.ics.cameo.proto.Messages.ShowAllCommand;
import fr.ill.ics.cameo.proto.Messages.ShowStreamCommand;
import fr.ill.ics.cameo.proto.Messages.StartCommand;
import fr.ill.ics.cameo.proto.Messages.StopCommand;
import fr.ill.ics.cameo.proto.Messages.SubscribePublisherCommand;

/**
 * The server class is thread-safe except for the connect and terminate methods that must be called respectively 
 * before and after any concurrent calls.
 * @author legoc
 *
 */
public class ServerImpl extends ServicesImpl {

	private ConcurrentLinkedDeque<EventListener> eventListeners = new ConcurrentLinkedDeque<EventListener>(); 
	private EventThread eventThread;
	private Socket cancelPublisher;
		
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
		
		init();
		
		// create a cancel publisher so that it sends the CANCEL message to the status subscriber (connected to 2 publishers)
		cancelPublisher = context.createSocket(ZMQ.PUB);
		cancelPublisher.bind(getCancelEndpoint());
		
		// start the status thread if it is possible.
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
		
			// sending the CANCEL message to the status socket to stop it
			cancelPublisher.sendMore(CANCEL);
			cancelPublisher.send("cancel");
			
			try {
				eventThread.join();
				
			} catch (InterruptedException e) {
			}
		}
	}

	public void terminate() {

		terminateStatusThread();
		super.terminate();
	}
	
	public void registerStatusListener(EventListener listener) {
		eventListeners.add(listener);
	}
	
	public void unregisterStatusListener(EventListener listener) {
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
		
		ZMsg request = createStartRequest(name, args, instanceReference);
		ZMsg reply = tryRequest(request);
		byte[] messageData = reply.getFirst().getData();
		RequestResponse requestResponse = null;
		
		try {
			requestResponse = RequestResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		return new fr.ill.ics.cameo.impl.Response(requestResponse.getValue(), requestResponse.getMessage());
	}
	
	private int getStreamPort(String name) throws ConnectionTimeout {
		
		ZMsg request = createOutputRequest(name);
		ZMsg reply = tryRequest(request);
		byte[] messageData = reply.getFirst().getData();
		RequestResponse requestResponse = null;
		
		try {
			requestResponse = RequestResponse.parseFrom(messageData);
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		return requestResponse.getValue();
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
		
		// we set the name of the application and register before starting because the id is not available
		instance.setName(name);
		registerStatusListener(instance);
		
		try {
			// we connect to the stream port before starting the application
			// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly
			if (outputStream) {
				instance.setOutputStreamSocket(createOutputStreamSocket(getStreamPort(name)));
			}
			
			Response response = startApplication(name, args, instanceReference);
			
			if (response.getValue() == -1) {
				instance.setErrorMessage(response.getMessage());
			} else {
				instance.setId(response.getValue());
			}

		} catch (ConnectionTimeout e) {
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
		
		boolean outputStream = ((options & Option.OUTPUTSTREAM) != 0);
		
		InstanceImpl instance = new InstanceImpl(this);
		
		// we set the name of the application and register before starting because the id is not available
		instance.setName(name);
		registerStatusListener(instance);
		
		try {
			// we connect to the stream port before starting the application
			// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly
			if (outputStream) {
				instance.setOutputStreamSocket(createOutputStreamSocket(getStreamPort(name)));
			}
			
			Response response = startApplication(name, null, instanceReference);
			
			if (response.getValue() == -1) {
				instance.setErrorMessage(response.getMessage());
			} else {
				instance.setId(response.getValue());
			}
			
		} catch (ConnectionTimeout e) {
			instance.setErrorMessage(e.getMessage());
		}	
		
		return instance;
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
		// create msg
		ZMsg request;
		
		if (immediately) {
			request = createKillRequest(id);
		} else {
			request = createStopRequest(id);
		}
		
		ZMsg reply = tryRequest(request);
		
		byte[] messageData = reply.getFirst().getData();
		RequestResponse response = null;
		
		try {
			response = RequestResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		return new fr.ill.ics.cameo.impl.Response(id, response.getMessage());
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
		
		ZMsg request = createConnectRequest(name);
		ZMsg reply = tryRequest(request);

		try {
			ApplicationInfoListResponse response = ApplicationInfoListResponse.parseFrom(reply.getFirst().getData());
			LinkedList<Messages.ApplicationInfo> protoList = new LinkedList<Messages.ApplicationInfo>();
			for (int i = 0; i < response.getApplicationInfoCount(); i++) {
				protoList.add(response.getApplicationInfo(i));
			}
			Iterator<Messages.ApplicationInfo> it = protoList.iterator();
			while (it.hasNext()) {
				Messages.ApplicationInfo applicationInfo = (Messages.ApplicationInfo) it.next();
				
				InstanceImpl instance = new InstanceImpl(this);
				
				// we set the name of the application and register before starting because the id is not available
				instance.setName(name);
				registerStatusListener(instance);
				
				int applicationId = applicationInfo.getId();
				
				// test if the application is still alive otherwise we could have missed a status message
				if (isAlive(applicationId)) {
					// we connect to the stream port before starting the application
					// so that we are sure that the ENDSTREAM message will be received even if the application terminates rapidly
					if (outputStream) {
						instance.setOutputStreamSocket(createOutputStreamSocket(getStreamPort(name)));
					}
										
					instance.setId(applicationId);
					instance.setInitialState(applicationInfo.getApplicationState());
					instance.setPastStates(applicationInfo.getPastApplicationStates());
					
					instances.add(instance);
					
				} else {
					// it is important not to forget to unregister the result, otherwise a memory leak will occur
					unregisterStatusListener(instance);
				}
			}

		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
			
		} catch (ConnectionTimeout e) {
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

		ZMsg request = createAllAvailableRequest();
		ZMsg reply = tryRequest(request);
		LinkedList<Application.Configuration> applications = new LinkedList<Application.Configuration>();

		try {
			AllAvailableResponse response = AllAvailableResponse.parseFrom(reply.getFirst().getData());
			LinkedList<Messages.ApplicationConfig> protoList = new LinkedList<Messages.ApplicationConfig>();
			for (int i = 0; i < response.getApplicationConfigCount(); i++) {
				protoList.add(response.getApplicationConfig(i));
			}
			Iterator<Messages.ApplicationConfig> it = protoList.iterator();
			while (it.hasNext()) {
				Messages.ApplicationConfig config = (Messages.ApplicationConfig) it.next();
				applications.add(new Application.Configuration(config.getName(), config.getDescription(), config.getRunsSingle(), config.getRestart(), config.getStartingTime(), config.getRetries(), config.getStoppingTime()));
			}

		} catch (InvalidProtocolBufferException e) {
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

		ZMsg request = createShowAllRequest();
		ZMsg reply = tryRequest(request);
		LinkedList<Application.Info> applications = new LinkedList<Application.Info>();

		try {
			ApplicationInfoListResponse response = ApplicationInfoListResponse.parseFrom(reply.getFirst().getData());
			LinkedList<Messages.ApplicationInfo> protoList = new LinkedList<Messages.ApplicationInfo>();
			for (int i = 0; i < response.getApplicationInfoCount(); i++) {
				protoList.add(response.getApplicationInfo(i));
			}
			Iterator<Messages.ApplicationInfo> it = protoList.iterator();
			while (it.hasNext()) {
				Messages.ApplicationInfo applicationInfo = (Messages.ApplicationInfo) it.next();
				applications.add(new Application.Info(applicationInfo.getName(), applicationInfo.getId(), applicationInfo.getApplicationState(), applicationInfo.getPastApplicationStates(), applicationInfo.getArgs()));
			}

		} catch (InvalidProtocolBufferException e) {
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
		Socket subscriber = context.createSocket(ZMQ.SUB);
		
		subscriber.connect(url + ":" + port);
		subscriber.subscribe(STREAM.getBytes());
		subscriber.subscribe(ENDSTREAM.getBytes());
		
		return new OutputStreamSocket(STREAM, ENDSTREAM, context, subscriber);
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

		ZMsg request = createShowStreamRequest(id);
		ZMsg reply = tryRequest(request);
		
		byte[] messageData = reply.getFirst().getData();
		RequestResponse requestResponse = null;
		try {
			requestResponse = RequestResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		int port = requestResponse.getValue();
		
		// in case of error, the returned value is -1
		if (port == -1) {
			throw new OutputStreamException(requestResponse.getMessage());
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

		ZMsg request = createIsAliveRequest(id);
		ZMsg reply = tryRequest(request);
		IsAliveResponse requestResponse = null;
		byte[] messageData = reply.getFirst().getData();
		
		try {
			requestResponse = IsAliveResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		return requestResponse.getIsAlive();
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
	public void writeToInputStream(int id, String[] parametersArray) throws WriteException {

		ZMsg request = createSendParametersRequest(id, parametersArray);
		ZMsg reply = tryRequest(request);
		byte[] messageData = reply.getFirst().getData();
		RequestResponse requestResponse = null;
		
		try {
			requestResponse = RequestResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		if (requestResponse.getValue() == -1) {
			throw new WriteException(requestResponse.getMessage());
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
	public void writeToInputStream(int id, String parameters) throws WriteException {
		
		String[] parametersArray = new String[1];
		parametersArray[0] = parameters;
		
		writeToInputStream(id, parametersArray);
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
		
		ZMsg request = createConnectPublisherRequest(applicationId, publisherName);
		ZMsg reply = tryRequest(request);
		byte[] messageData = reply.getFirst().getData();
		PublisherResponse requestResponse = null;
		
		try {
			requestResponse = PublisherResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		int publisherPort = requestResponse.getPublisherPort();
		if (publisherPort == -1) {
			throw new SubscriberCreationException(requestResponse.getMessage());
		}
			
		int synchronizerPort = requestResponse.getSynchronizerPort();
		int numberOfSubscribers = requestResponse.getNumberOfSubscribers();
		
		SubscriberImpl subscriber = new SubscriberImpl(this, context, url, publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instance);
		subscriber.init();
		
		return subscriber;
	}
	
	void subscribeToPublisher(String endpoint) throws ConnectionTimeout {
		
		ZMsg request = createSubscribePublisherRequest();
		ZMsg reply = tryRequest(request, endpoint);
		
		byte[] messageData = reply.getFirst().getData();
		RequestResponse requestResponse = null;
		
		try {
			requestResponse = RequestResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	/**
	 * create isAlive request
	 * 
	 * @param text
	 * @return
	 */
	private ZMsg createIsAliveRequest(int id) {
		
		ZMsg request = createRequest(Type.ISALIVE);
		IsAliveCommand isAlive = IsAliveCommand.newBuilder().setId(id).build();
		request.add(isAlive.toByteArray());
		
		return request;
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
	private ZMsg createStartRequest(String name, String[] args, String instanceReference) {
		
		ZMsg request = createRequest(Type.START);
		StartCommand command = null;
		
		if (args == null) {
			command = StartCommand.newBuilder()
									.setName(name)
									.setInstanceReference(instanceReference)
									.build();
		} else {
			
			LinkedList<String> list = new LinkedList<String>();

			for (int i = 0; i < args.length; i++) {
				list.add(args[i]);
			}
			command = StartCommand.newBuilder()
									.setName(name)
									.addAllArgs(list)
									.setInstanceReference(instanceReference)
									.build();
		}
		request.add(command.toByteArray());
		
		return request;
	}

	/**
	 * create stop request
	 * 
	 * @param id
	 * @return request
	 */
	private ZMsg createStopRequest(int id) {

		ZMsg request = createRequest(Type.STOP);
		StopCommand stop = StopCommand.newBuilder().setId(id).build();
		request.add(stop.toByteArray());
		
		return request;
	}
	
	/**
	 * create kill request
	 * 
	 * @param id
	 * @return request
	 */
	private ZMsg createKillRequest(int id) {

		ZMsg request = createRequest(Type.KILL);
		KillCommand kill = KillCommand.newBuilder().setId(id).build();
		request.add(kill.toByteArray());

		return request;
	}

	/**
	 * create connect request
	 * 
	 * @param name
	 * @param argsOfApplication
	 * @return request
	 */
	private ZMsg createConnectRequest(String name) {
		ZMsg request = createRequest(Type.CONNECT);
		ConnectCommand connect = ConnectCommand.newBuilder().setName(name).build();
		request.add(connect.toByteArray());
		
		return request;
	}
	
	/**
	 * create all available request
	 * 
	 * @return request
	 */
	private ZMsg createAllAvailableRequest() {

		ZMsg request = createRequest(Type.ALLAVAILABLE);
		AllAvailableCommand allAvailable = AllAvailableCommand.newBuilder().build();
		request.add(allAvailable.toByteArray());
		
		return request;
	}
	
	/**
	 * create showall request
	 * 
	 * @return request
	 */
	private ZMsg createShowAllRequest() {

		ZMsg request = createRequest(Type.SHOWALL);
		ShowAllCommand showAll = ShowAllCommand.newBuilder().build();
		request.add(showAll.toByteArray());
		
		return request;
	}

	/**
	 * create showall request
	 * 
	 * @return request
	 */
	private ZMsg createShowStreamRequest(int id) {
		
		ZMsg request = createRequest(Type.SHOW);
		ShowStreamCommand command = ShowStreamCommand.newBuilder().setId(id).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	/**
	 * create SendParameters request
	 * 
	 * @param id
	 * @param parametersArray
	 * @return
	 */
	private ZMsg createSendParametersRequest(int id, String[] parametersArray) {
		
		ZMsg request = createRequest(Type.SENDPARAMETERS);

		LinkedList<String> list = new LinkedList<String>();
		for (int i = 0; i < parametersArray.length; i++) {
			list.add(parametersArray[i]);
		}

		SendParametersCommand command = SendParametersCommand.newBuilder().setId(id).addAllParameters(list).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	/**
	 * create output request
	 * 
	 * @param name
	 */
	private ZMsg createOutputRequest(String name) {
		
		ZMsg request = createRequest(Type.OUTPUT);
		OutputCommand command = OutputCommand.newBuilder().setName(name).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	private ZMsg createConnectPublisherRequest(int applicationId, String publisherName) {
		
		ZMsg request = createRequest(Type.CONNECTPUBLISHER);
		ConnectPublisherCommand command = ConnectPublisherCommand.newBuilder().setApplicationId(applicationId).setPublisherName(publisherName).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	protected ZMsg createSubscribePublisherRequest() {
		
		ZMsg request = createRequest(Type.SUBSCRIBEPUBLISHER);
		SubscribePublisherCommand command = SubscribePublisherCommand.newBuilder().build();
		request.add(command.toByteArray());
		
		return request;
	}

	@Override
	public String toString() {
		return "server@" + serverEndpoint;
	}
}