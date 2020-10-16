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

package fr.ill.ics.cameo.manager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Context;
import fr.ill.ics.cameo.exception.ApplicationAlreadyExecuting;
import fr.ill.ics.cameo.exception.IdNotFoundException;
import fr.ill.ics.cameo.exception.MaxNumberOfApplicationsReached;
import fr.ill.ics.cameo.exception.StreamNotPublishedException;
import fr.ill.ics.cameo.exception.UnknownApplicationException;
import fr.ill.ics.cameo.exception.UnknownPublisherException;
import fr.ill.ics.cameo.exception.UnmanagedApplicationException;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.threads.LifecycleApplicationThread;
import fr.ill.ics.cameo.threads.StreamApplicationThread;

public class Manager extends ConfigLoader {

	private ConcurrentHashMap<Integer, Application> applicationMap;
	private static int MAX_ID = 65536; 
	private int maxId = 0;
	private Zmq.Socket eventPublisher;
	private HashMap<String, Zmq.Socket> streamPublishers = new HashMap<String, Zmq.Socket>();

	private final static String PUBLISHER_PREFIX = "pub.";
	private final static String SYNCHRONIZER_PREFIX = "sync.";
	private final static String RESPONDER_PREFIX = "rep.";
	private final static String REQUESTER_PREFIX = "req.";
	
	public Manager(String xmlPath) {
		super(xmlPath);
		Log.init();
		Log.logger().info("Endpoint is " + ConfigManager.getInstance().getHostEndpoint());
		
		showApplicationConfigs();
		
		applicationMap = new ConcurrentHashMap<Integer, Application>();
		
		// security test
		if (ConfigManager.getInstance().getMaxNumberOfApplications() > MAX_ID) {
			MAX_ID = ConfigManager.getInstance().getMaxNumberOfApplications();
		}
		
		Log.logger().fine("Max Id is " + MAX_ID);
	}

	public Manager(InputStream configStream) {
		super(configStream);
		Log.init();
		Log.logger().info("Endpoint is " + ConfigManager.getInstance().getHostEndpoint());
		
		showApplicationConfigs();
		
		applicationMap = new ConcurrentHashMap<Integer, Application>();
		
		// security test
		if (ConfigManager.getInstance().getMaxNumberOfApplications() > MAX_ID) {
			MAX_ID = ConfigManager.getInstance().getMaxNumberOfApplications();
		}
		
		Log.logger().fine("Max Id is " + MAX_ID);
	}

	public synchronized void initStreamSockets(Context context) {
		
		eventPublisher = context.createSocket(Zmq.PUB);
		
		int port = ConfigManager.getInstance().getStreamPort();
		eventPublisher.bind("tcp://*:" + port);
		
		Log.logger().info("Status socket on port " + port);
		
		// iterate the application configurations
		for (ApplicationConfig c : applicationList) {
			Zmq.Socket streamPublisher = context.createSocket(Zmq.PUB);
			if (c.hasStream()) {
				port = c.getStreamPort();
				streamPublisher.bind("tcp://*:" + port);
				
				streamPublishers.put(c.getName(), streamPublisher);
				
				Log.logger().info("Application " + c.getName() + " output socket on port " + port);
			}	
		}
	}

	public Zmq.Socket getStreamPublisher(String name) {
		return streamPublishers.get(name);
	}
	
	public static void publishSynchronized(Zmq.Socket publisher, String type, byte[] data) {
		
		synchronized (publisher) {
			publisher.sendMore(type);
			publisher.send(data, 0);
		}
	}

	public synchronized void sendStatus(int id, String name, int state, int pastStates, int exitCode) {
		
		JSONObject event = new JSONObject();
		event.put(Message.StatusEvent.ID, id);
		event.put(Message.StatusEvent.NAME, name);
		event.put(Message.StatusEvent.APPLICATION_STATE, state);
		event.put(Message.StatusEvent.PAST_APPLICATION_STATES, pastStates);
		
		if (exitCode != -1) {
			event.put(Message.StatusEvent.EXIT_CODE, exitCode);
		}
		
		eventPublisher.sendMore(Message.Event.STATUS);
		eventPublisher.send(Message.serialize(event), 0);
	}

	public synchronized void sendResult(int id, String name, byte[] data) {
		
		JSONObject event = new JSONObject();
		event.put(Message.ResultEvent.ID, id);
		event.put(Message.ResultEvent.NAME, name);

		// The result has 3 parts.
		eventPublisher.sendMore(Message.Event.RESULT);
		eventPublisher.sendMore(Message.serialize(event));
		eventPublisher.send(data, 0);
	}
	
	public synchronized void sendPublisher(int id, String name, String publisherName) {
		
		JSONObject event = new JSONObject();
		event.put(Message.PublisherEvent.ID, id);
		event.put(Message.PublisherEvent.NAME, name);
		event.put(Message.PublisherEvent.PUBLISHER_NAME, publisherName);
		
		eventPublisher.sendMore(Message.Event.PUBLISHER);
		eventPublisher.send(Message.serialize(event), 0);
	}
	
	public synchronized void sendPort(int id, String name, String portName) {
		
		JSONObject event = new JSONObject();
		event.put(Message.PortEvent.ID, id);
		event.put(Message.PortEvent.NAME, name);
		event.put(Message.PortEvent.PORT_NAME, portName);
		
		eventPublisher.sendMore(Message.Event.PORT);
		eventPublisher.send(Message.serialize(event), 0);
	}
	
	public synchronized void sendStoreKeyValue(int id, String name, String key, String value) {
		
		JSONObject event = new JSONObject();
		event.put(Message.KeyEvent.ID, id);
		event.put(Message.KeyEvent.NAME, name);
		event.put(Message.KeyEvent.STATUS, Message.STORE_KEY_VALUE);
		event.put(Message.KeyEvent.KEY, key);
		event.put(Message.KeyEvent.VALUE, value);
		
		eventPublisher.sendMore(Message.Event.KEYVALUE);
		eventPublisher.send(Message.serialize(event), 0);
	}
	
	public synchronized void sendRemoveKeyValue(int id, String name, String key, String value) {
		
		JSONObject event = new JSONObject();
		event.put(Message.KeyEvent.ID, id);
		event.put(Message.KeyEvent.NAME, name);
		event.put(Message.KeyEvent.STATUS, Message.REMOVE_KEY);
		event.put(Message.KeyEvent.KEY, key);
		event.put(Message.KeyEvent.VALUE, value);
		
		eventPublisher.sendMore(Message.Event.KEYVALUE);
		eventPublisher.send(Message.serialize(event), 0);
	}
	
	private int findFreeId(int begin, int end) {
		
		for (int i = begin; i < end; i++) {
			if (!applicationMap.containsKey(i)) {
				return i;
			}
		}
		
		return -1;
	}

	private int findId() throws MaxNumberOfApplicationsReached {

		// First iteration
		int id = findFreeId(maxId + 1, MAX_ID + 1);
		if (id != -1) {
			// Found an id
			maxId = id;
		} else {
			// Found no id, iterate from the beginning to maxId
			id = findFreeId(1, maxId + 1);
			if (id != -1) {
				// Found an id
				maxId = id;
			}
		}
		
		if (id == -1) {
			Log.logger().info("Max number of applications reached");
			throw new MaxNumberOfApplicationsReached();
		}
		
		return id;
	}
	
	private void removeApplication(Application application) {
		
		// Iterate on the ports of the application to remove them.
		HashMap<String, Integer> ports = application.getPorts();
		for (Entry<String, Integer> e : ports.entrySet()) {
			PortManager.getInstance().removePort(e.getValue());	
		}
				
		applicationMap.remove(application.getId());
	}
	
	/**
	 * start application
	 * 
	 * @param commandArray
	 * @param args
	 * @param serverEndpoint 
	 * @param result 
	 * @return
	 * @throws UnknownApplicationException
	 * @throws MaxNumberOfApplicationsReached
	 * @throws ApplicationAlreadyExecuting
	 */
	public synchronized Application startApplication(String name, String[] args, String starterReference) throws UnknownApplicationException, MaxNumberOfApplicationsReached, ApplicationAlreadyExecuting {
		
		ApplicationConfig config = this.verifyApplicationExistence(name);
		Log.logger().fine("Trying to start " + name);

		// Verify if the application is already running
		verifyNumberOfInstances(config.getName(), config.runsSingle());

		// Find an id, throws an exception if there is no id available.
		int id = findId();
		
		// Create application
		Application application = new ManagedApplication(ConfigManager.getInstance().getHostEndpoint(), id, config, args, starterReference);
		applicationMap.put(id, application);
		
		// Threads
		// Verifiy application thread
		LifecycleApplicationThread lifecycleThread = new LifecycleApplicationThread(application, this, Log.logger());
		lifecycleThread.start();
		
		// Stream thread
		if (application.isWriteStream() || application.hasStream()) {
			if (application.getLogPath() != null) {
				Log.logger().fine("Application " + application.getNameId() + " has stream to log file '" + application.getLogPath() + "'");
			} else {
				Log.logger().fine("Application " + application.getNameId() + " has stream");
			}

			// The thread is built but not started here because it requires that the process is started
			StreamApplicationThread streamThread = new StreamApplicationThread(application, this);
			application.setStreamThread(streamThread);
		}
		
		return application;
	}

	/**
	 * stop application
	 * 
	 * @param id
	 * @throws IdNotFoundException
	 */
	public synchronized String stopApplication(int id) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			
			String name = application.getName();
			
			// If the process is dead, there is no thread
			if (application.getProcessState().equals(ProcessState.DEAD)) {
				removeApplication(application);
			} else {
				// The following call will have no effect if it was already called.
				application.setHasToStop(true, false);
			}
			return name;
			
		} else {
			throw new IdNotFoundException();
		}
	}
	
	/**
	 * kill application
	 * 
	 * @param id
	 * @throws IdNotFoundException
	 */
	public synchronized String killApplication(int id) throws IdNotFoundException {

		Application application = applicationMap.get(id);
		
		if (application != null) {
			
			String name = application.getName();
			
			// if process is dead, there is not thread on it
			if (application.getProcessState().equals(ProcessState.DEAD)) {
				removeApplication(application);
			} else {
				application.setHasToStop(true, true);
			}
			return name;
			
		} else {
			throw new IdNotFoundException();
		}
	}
	
	/**
	 * kill application
	 * 
	 * @param id
	 * @throws IdNotFoundException
	 */
	public synchronized void killAllApplications() {

		for (Application application : applicationMap.values()) {
		
			if (application != null) {
				
				// if process is dead, there is not thread on it
				if (application.getProcessState().equals(ProcessState.DEAD)) {
					removeApplication(application);
				} else {
					application.setHasToStop(true, true);
				}
				application.kill();
			}
		}
	}
	
	/**
	 * show process, and add them to a reply
	 * 
	 * @return reply with running appli
	 */
	public synchronized LinkedList<ApplicationInfo> showApplicationMap() {
		LinkedList<ApplicationInfo> list = new LinkedList<ApplicationInfo>();
		Log.logger().fine("Showing applications");
		String args = null;

		for (java.util.Map.Entry<Integer, Application> entry : applicationMap.entrySet()) {
			Application application = entry.getValue();
			if (application.getArgs() == null) {
				args = "no arguments";
			} else {
				args = String.join(" ", application.getArgs());
			}
			
			ApplicationInfo applicationInfo = new ApplicationInfo(application.getId(),
												application.getPid(),
												application.getApplicationState(), 
												application.getPastApplicationStates(),
												args, 
												application.hasToStop(), 
												application.hasStream(), 
												application.isWriteStream(), 
												application.runsSingle(), 
												application.isRestart(), 
												application.isPassInfo(), 
												application.getName(), 
												application.getStartExecutable(), 
												application.getStartingTime(), 
												application.getLogPath(), 
												application.getStoppingTime(), 
												application.getStopExecutable());
			list.add(applicationInfo);
		}
		return list;
	}

	/**
	 * show stream from appli
	 * 
	 * @param stringId
	 * @param stream
	 * @param argsForAppli
	 * @throws IdNotFoundException
	 * @throws UnknownApplicationException
	 * @throws StreamNotPublishedException 
	 */
	public synchronized int getStreamPort(int id) throws IdNotFoundException, UnknownApplicationException, StreamNotPublishedException {
		
		// find application
		if (applicationMap.containsKey(id)) {
			Application application = applicationMap.get(id);
						
			Log.logger().fine("Application " + application.getNameId() + " has stream port " + application.getStreamPort());
			
			return application.getStreamPort();
			
		} else {
			throw new IdNotFoundException();
		}

	}
	
	/**
	 * verify if an application already run
	 * 
	 * @param config
	 * @throws ApplicationAlreadyExecuting
	 * @throws MaxNumberOfApplicationsReached 
	 */
	private void verifyNumberOfInstances(String name, boolean single) throws ApplicationAlreadyExecuting, MaxNumberOfApplicationsReached {
		
		// count the application instances
		int counter = 0;
		
		// just check name
		for (java.util.Map.Entry<Integer, Application> entry : applicationMap.entrySet()) {
		
			Application application = entry.getValue();
			
			if (application.getName().equals(name)) {
				
				// check if application is dead
				if (application.getProcessState() == ProcessState.DEAD) {
					removeApplication(application);
				}
				else {
					// increment the counter
					++counter;
					
					if (single) {
						Log.logger().info("Application with name " + application.getName() + " is already executing with id " + application.getId());
				
						throw new ApplicationAlreadyExecuting();
					}
					else if (counter >= ConfigManager.getInstance().getMaxNumberOfApplications()) {
						Log.logger().info("Max number of applications reached");
						
						throw new MaxNumberOfApplicationsReached();
					}
				}
			}
		}
	}

	/**
	 * is used to know if an application is already in process
	 * 
	 * @param id
	 * @return
	 */
	public synchronized boolean isAlive(int id) {
		
		if (applicationMap.containsKey(id)) {
			Application application = applicationMap.get(id);
			int state = application.getApplicationState();
			
			if (state == ApplicationState.STARTING
				|| state == ApplicationState.RUNNING
				|| state == ApplicationState.STOPPING) {
				return true;
			}
			return false;
			
		} else {
			return false;
		}
	}

	/**
	 * send parameters to an application
	 * 
	 * @param id
	 * @param parametersToSend
	 * @throws IdNotFoundException
	 * @throws UnmanagedApplicationException 
	 */
	public synchronized void writeToInputStream(int id, String[] parametersToSend) throws IdNotFoundException, UnmanagedApplicationException {
		
		if (applicationMap.containsKey(id)) {
			Application application = applicationMap.get(id);
			
			// The process can be null in case it is an unmanaged application.
			if (application.getProcess() == null) {
				// We should throw a specific exception.
				throw new UnmanagedApplicationException();
			}
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(application.getProcess().getOutputStream()));
			
			// build simple string from parameters
			String parametersString = "";
			for (int i = 0; i < parametersToSend.length - 1; i++) {
				parametersString += parametersToSend[i] + " ";
			}
			parametersString += parametersToSend[parametersToSend.length - 1];
			
			// send the parameters string
			try {
				writer.write(parametersString);
				writer.flush();
				
			} catch (IOException e) {
				Log.logger().severe("Enable to send parameters to application " + application.getNameId());
				try {
					writer.close();
				} catch (IOException ec) {
					// do nothing
				}
			}
		
		} else {
			throw new IdNotFoundException();
		}
	}
	
	public synchronized void setApplicationState(Application application, int applicationState, int exitValue) {
		
		// states are : UNKNOWN, STARTING, RUNNING, STOPPING, KILLING, PROCESSING_ERROR, ERROR, SUCCESS, STOPPED, KILLED
		// set the status of the application
		application.setState(applicationState);
		
		// send the status
		sendStatus(application.getId(), application.getName(), applicationState, application.getPastApplicationStates(), exitValue);
				
		// remove the application for terminal states
		if (applicationState == ApplicationState.ERROR
			|| applicationState == ApplicationState.STOPPED
			|| applicationState == ApplicationState.KILLED
			|| applicationState == ApplicationState.SUCCESS) {
			
			removeApplication(application);
		}
	}
	
	public synchronized void setApplicationState(Application application, int applicationState) {
		setApplicationState(application, applicationState, -1);
	}

	public synchronized void setApplicationProcessState(Application application, ProcessState processState) {
		application.setProcessState(processState);
	}
			
	public synchronized void startApplicationStreamThread(Application application) {
		// process is alive
		application.startStreamThread();			
	}

	public synchronized void resetApplicationStreamThread(Application application) {
		application.setStreamThread(null);
	}

	public synchronized boolean setApplicationStateFromClient(int id, int state) throws IdNotFoundException {

		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		int currentState = application.getApplicationState();
		
		// states are : UNKNOWN, STARTING, RUNNING, STOPPING, KILLING, PROCESSING_ERROR, ERROR, SUCCESS, STOPPED, KILLED
		// state that can be set by the client : RUNNING
		if (state == ApplicationState.RUNNING) {
			// current state can only be STARTING
			if (currentState == ApplicationState.STARTING) {
				setApplicationState(application, state);
				return true;
			} else if (currentState == ApplicationState.RUNNING) {
				// do not change the state, but it is ok
				return true;
			} else {
				return false;
			}
		}
		
		return false;		
	}
	

	public synchronized boolean setApplicationResult(int id, byte[] data) throws IdNotFoundException {
	
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);

		// send the result that is not stored
		sendResult(application.getId(), application.getName(), data);
		
		return true;
	}
	
	public synchronized StatusInfo getApplicationState(int id) {
		
		StatusInfo status = new StatusInfo();
		status.setId(id);
		
		if (!applicationMap.containsKey(id)) {
			status.setName("?");
			status.setApplicationState(ApplicationState.UNKNOWN);
			status.setPastApplicationStates(0);
		}
		else {
			Application application = applicationMap.get(id);
			status.setName(application.getName());
			status.setApplicationState(application.getApplicationState());
			status.setPastApplicationStates(application.getPastApplicationStates());
		}
		
		return status;
	}
	
	public synchronized void sendEndOfStream(Application application) {
		application.sendEndOfStream();
	}

	public synchronized int requestPortForApplication(int id, String portName) throws IdNotFoundException {
		
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
	
		Application application = applicationMap.get(id);
		HashMap<String, Integer> ports = application.getPorts();
		
		if (ports.containsKey(portName)) {
			// The port already exists.
			return -1;
		}
		
		int port = PortManager.getInstance().requestPort();
		ports.put(portName, port);

		sendPort(id, application.getName(), portName);
		
		Log.logger().info("Application " + application.getNameId() + " has socket " + portName + " on port " + port);
		
		return port;
	}
	
	public synchronized int connectPortForApplication(int id, String portName) throws IdNotFoundException {
		
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		
		HashMap<String, Integer> ports = application.getPorts();
		
		if (ports.containsKey(portName)) {
			return ports.get(portName);
		}
		
		return -1;
	}
	
	public synchronized boolean removePortForApplication(int id, String portName) throws IdNotFoundException {
		
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		HashMap<String, Integer> ports = application.getPorts();
		
		if (ports.containsKey(portName)) {
			PortManager.getInstance().removePort(ports.get(portName));
			ports.remove(portName);
			Log.logger().info("Application " + application.getNameId() + " removed socket " + portName);
			
			return true;
		}

		Log.logger().info("Application " + application.getNameId() + " cannot remove socket " + portName);
		
		return false;
	}

	public synchronized int[] createPublisherForApplication(int id, String publisherName, int numberOfSubscribers) throws IdNotFoundException {
		
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		HashMap<String, Application.Publisher> publishers = application.getPublishers();
		
		if (publishers.containsKey(publisherName)) {
			return null;
		}
		
		// create 2 new ports because we need:
		// - publisher port
		// - synchronizer port
		int publisherPort = PortManager.getInstance().requestPort();
		int synchronizerPort = PortManager.getInstance().requestPort();

		HashMap<String, Integer> ports = application.getPorts();
		
		ports.put(PUBLISHER_PREFIX + publisherName, publisherPort);
		ports.put(SYNCHRONIZER_PREFIX + publisherName, synchronizerPort);
		
		Application.Publisher publisher = new Application.Publisher();
		publisher.numberOfSubscribers = numberOfSubscribers;
		
		publishers.put(publisherName, publisher);

		Log.logger().info("Application " + application.getNameId() + " has publisher socket on ports " + publisherPort + " and " + synchronizerPort);

		// send the event
		sendPublisher(id, application.getName(), publisherName);
		
		int[] result = new int[2];
		result[0] = publisherPort;
		result[1] = synchronizerPort;
		
		return result;
	}
	
	public synchronized boolean terminatePublisherForApplication(int id, String publisherName) throws IdNotFoundException {
		
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		HashMap<String, Application.Publisher> publishers = application.getPublishers();
		
		Application.Publisher publisher = publishers.get(publisherName);
		
		if (publisher != null) {
			// Remove the two ports
			HashMap<String, Integer> ports = application.getPorts();
			
			int publisherPort = ports.get(PUBLISHER_PREFIX + publisherName);
			int synchronizerPort = ports.get(SYNCHRONIZER_PREFIX + publisherName);
			
			PortManager.getInstance().removePort(publisherPort);
			PortManager.getInstance().removePort(synchronizerPort);
			publishers.remove(publisherName);
			
			Log.logger().info("Application " + application.getNameId() + " closed publisher socket " + publisherName + " on ports " + publisherPort + " and " + synchronizerPort);
			
			return true;
		}
		
		Log.logger().info("Application " + application.getNameId() + " cannot close publisher socket " + publisherName);
		
		return false;
	}

	public synchronized Application.Publisher getPublisherForApplication(int id, String publisherName) throws UnknownPublisherException, IdNotFoundException {

		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		HashMap<String, Application.Publisher> publishers = application.getPublishers();
		
		if (!publishers.containsKey(publisherName)) {
			throw new UnknownPublisherException();
		}
		
		return publishers.get(publisherName);
	}
	
	public synchronized int[] getPublisherPortsForApplication(int id, String publisherName) throws UnknownPublisherException, IdNotFoundException {

		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		HashMap<String, Integer> ports = application.getPorts();
		
		int[] result = new int[2];
		
		String publisherKey = PUBLISHER_PREFIX + publisherName;
		
		if (!ports.containsKey(publisherKey)) {
			throw new UnknownPublisherException();
		}
		
		String synchronizerKey = SYNCHRONIZER_PREFIX + publisherName;
		
		if (!ports.containsKey(synchronizerKey)) {
			throw new UnknownPublisherException();
		}
		
		result[0] = ports.get(publisherKey);
		result[1] = ports.get(synchronizerKey);
				
		return result;
	}

	public int newStartedUnmanagedApplication(String name, long pid) throws MaxNumberOfApplicationsReached, ApplicationAlreadyExecuting {
		
		// Verify if the application is already running
		verifyNumberOfInstances(name, false);
		
		// Find an id, throws an exception if there is no id available.
		int id = findId();
		
		// Create application
		Application application = new UnmanagedApplication(ConfigManager.getInstance().getHostEndpoint(), id, name, pid);
		applicationMap.put(id, application);
		
		// Threads
		// Verifiy application thread
		LifecycleApplicationThread verifyThread = new LifecycleApplicationThread(application, this, Log.logger());
		verifyThread.start();
		
		Log.logger().fine("Application " + application.getNameId() + " is started");
		
		// No stream thread.
		return id;
	}
	
	public int newStartedUnmanagedApplication(String name) throws MaxNumberOfApplicationsReached, ApplicationAlreadyExecuting {
		return newStartedUnmanagedApplication(name, 0);
	}

	public String setUnmanagedApplicationTerminated(int id) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			
			String name = application.getName();
			
			// Terminate the application that is unmanaged.
			((UnmanagedApplication)application).terminate();
			
			// Remove the application.
			removeApplication(application);
			
			Log.logger().info("Application " + application.getNameId() + " is terminated");
			
			return name;
			
		} else {
			throw new IdNotFoundException();
		}
	}

	public void storeKeyValue(int id, String key, String value) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			application.storeKeyValue(key, value);
			
			// Send the event.
			sendStoreKeyValue(id, application.getName(), key, value);
			
		} else {
			throw new IdNotFoundException();
		}
	}

	public String getKeyValue(int id, String key) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			return application.getKeyValue(key);
			
		} else {
			throw new IdNotFoundException();
		}
	}

	public boolean removeKey(int id, String key) throws IdNotFoundException {

		Application application = applicationMap.get(id);
		
		if (application != null) {
			String value = application.getKeyValue(key);
			boolean removed = application.removeKey(key);
			
			if (removed) {
				// Send the event.
				sendRemoveKeyValue(id, application.getName(), key, value);
			}
			
			return removed;
			
		} else {
			throw new IdNotFoundException();
		}
	}
}