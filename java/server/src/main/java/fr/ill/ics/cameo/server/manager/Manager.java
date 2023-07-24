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

package fr.ill.ics.cameo.server.manager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.com.Zmq;
import fr.ill.ics.cameo.com.Zmq.Context;
import fr.ill.ics.cameo.common.messages.Messages;
import fr.ill.ics.cameo.common.strings.ApplicationIdentity;
import fr.ill.ics.cameo.common.strings.Endpoint;
import fr.ill.ics.cameo.server.exception.IdNotFoundException;
import fr.ill.ics.cameo.server.exception.KeyAlreadyExistsException;
import fr.ill.ics.cameo.server.exception.MaxGlobalNumberOfApplicationsReached;
import fr.ill.ics.cameo.server.exception.MaxNumberOfApplicationsReached;
import fr.ill.ics.cameo.server.exception.StreamNotPublishedException;
import fr.ill.ics.cameo.server.exception.UnknownApplicationException;
import fr.ill.ics.cameo.server.exception.UnregisteredApplicationException;
import fr.ill.ics.cameo.server.threads.LifecycleApplicationThread;
import fr.ill.ics.cameo.server.threads.StreamApplicationThread;

public class Manager extends ConfigLoader {

	private ConcurrentHashMap<Integer, Application> applicationMap;
	private static int MAX_ID = 65536; 
	private int maxId = 0;
	private Zmq.Socket eventPublisher;
	private HashMap<String, Zmq.Socket> streamPublishers = new HashMap<String, Zmq.Socket>();
	
	public Manager(String xmlPath) {
		super(xmlPath);
		Log.init();
		Log.logger().info("Endpoint is " + ConfigManager.getInstance().getHostEndpoint());
		
		displayApplicationConfigs();
		
		applicationMap = new ConcurrentHashMap<Integer, Application>();
		
		// Security test.
		if (ConfigManager.getInstance().getMaxNumberOfApplications() > MAX_ID) {
			MAX_ID = ConfigManager.getInstance().getMaxNumberOfApplications();
		}
		
		Log.logger().fine("Max Id is " + MAX_ID);
	}

	public Manager(InputStream configStream) {
		super(configStream);
		Log.init();
		Log.logger().info("Endpoint is " + ConfigManager.getInstance().getHostEndpoint());
		
		displayApplicationConfigs();
		
		applicationMap = new ConcurrentHashMap<Integer, Application>();
		
		// Security test.
		if (ConfigManager.getInstance().getMaxNumberOfApplications() > MAX_ID) {
			MAX_ID = ConfigManager.getInstance().getMaxNumberOfApplications();
		}
		
		Log.logger().fine("Max Id is " + MAX_ID);
	}

	private synchronized int initPublisher(Zmq.Socket socket, String applicationName) {
	
		// Check proxies.
		if (ConfigManager.getInstance().hasProxies()) {
		
			// Connect the socket to the proxy local endpoint as the proxy and this server run on the same host.
			Endpoint proxyEndpoint = ConfigManager.getInstance().getSubscriberProxyLocalEndpoint();
	
			try {
				socket.connect(proxyEndpoint.toString());
				
				Log.logger().info("Connected publisher " + applicationName + " to proxy " + proxyEndpoint);
			}
			catch (Exception e) {
				Log.logger().severe("Cannot connect to publisher proxy " + proxyEndpoint + ": " + e.getMessage());
				System.exit(1);
			}
		}
		
		// Loop until the socket is bound.
		while (true) {
			
			// Request a new port.
			int port = PortManager.getInstance().requestPort(applicationName, null);
			
			// Try to bind the port.
			try {
				socket.bind("tcp://*:" + port);
				return port;
			}
			catch (Exception e) {
				// The port is not available.
				PortManager.getInstance().setPortUnavailable(port);
			}
		}
	}
	
	public synchronized void initStreamSockets(Context context) {
		
		eventPublisher = context.createSocket(Zmq.PUB);
		
		int port = initPublisher(eventPublisher, "<server>:<event>");
		ConfigManager.getInstance().setStreamPort(port);
		
		Log.logger().info("Status socket on port " + port);
		
		// Iterate the application configurations.
		for (ApplicationConfig config : applicationList) {
			
			if (config.hasOutputStream()) {
				Zmq.Socket streamPublisher = context.createSocket(Zmq.PUB);
				
				port = initPublisher(streamPublisher, "<server>:<output>" + config.getName());
				config.setOutputStreamPort(port);
				
				streamPublishers.put(config.getName(), streamPublisher);
				
				Log.logger().info("Application " + config.getName() + " output socket on port " + port);
			}	
		}
	}

	public Zmq.Socket getStreamPublisher(String name) {
		return streamPublishers.get(name);
	}
	
	public static void publishSynchronized(Zmq.Socket publisher, String topicId, byte[] data) {
		
		synchronized (publisher) {
			publisher.sendMore(topicId);
			publisher.send(data, 0);
		}
	}

	public synchronized void sendStatus(int id, String name, int state, int pastStates, int exitCode) {
		
		JSONObject event = new JSONObject();
		event.put(Messages.StatusEvent.ID, id);
		event.put(Messages.StatusEvent.NAME, name);
		event.put(Messages.StatusEvent.APPLICATION_STATE, state);
		event.put(Messages.StatusEvent.PAST_APPLICATION_STATES, pastStates);
		
		if (exitCode != -1) {
			event.put(Messages.StatusEvent.EXIT_CODE, exitCode);
		}
		
		eventPublisher.sendMore(Messages.Event.STATUS);
		eventPublisher.send(Messages.serialize(event), 0);
	}

	public synchronized void sendResult(int id, String name, byte[] data) {
		
		JSONObject event = new JSONObject();
		event.put(Messages.ResultEvent.ID, id);
		event.put(Messages.ResultEvent.NAME, name);

		// The result has 3 parts.
		eventPublisher.sendMore(Messages.Event.RESULT);
		eventPublisher.sendMore(Messages.serialize(event));
		eventPublisher.send(data, 0);
	}
	
	public synchronized void sendStoreKeyValue(int id, String name, String key, String value) {
		
		JSONObject event = new JSONObject();
		event.put(Messages.KeyEvent.ID, id);
		event.put(Messages.KeyEvent.NAME, name);
		event.put(Messages.KeyEvent.STATUS, Messages.STORE_KEY_VALUE);
		event.put(Messages.KeyEvent.KEY, key);
		event.put(Messages.KeyEvent.VALUE, value);
		
		eventPublisher.sendMore(Messages.Event.KEYVALUE);
		eventPublisher.send(Messages.serialize(event), 0);
	}
	
	public synchronized void sendRemoveKeyValue(int id, String name, String key, String value) {
		
		JSONObject event = new JSONObject();
		event.put(Messages.KeyEvent.ID, id);
		event.put(Messages.KeyEvent.NAME, name);
		event.put(Messages.KeyEvent.STATUS, Messages.REMOVE_KEY);
		event.put(Messages.KeyEvent.KEY, key);
		event.put(Messages.KeyEvent.VALUE, value);
		
		eventPublisher.sendMore(Messages.Event.KEYVALUE);
		eventPublisher.send(Messages.serialize(event), 0);
	}
	
	private int findFreeId(int begin, int end) {
		
		for (int i = begin; i < end; i++) {
			if (!applicationMap.containsKey(i)) {
				return i;
			}
		}
		
		return -1;
	}

	private int findId(String name) throws MaxGlobalNumberOfApplicationsReached {

		// First iteration.
		int id = findFreeId(maxId + 1, MAX_ID + 1);
		if (id != -1) {
			// Found an id.
			maxId = id;
		}
		else {
			// Found no id, iterate from the beginning to maxId.
			id = findFreeId(1, maxId + 1);
			if (id != -1) {
				// Found an id.
				maxId = id;
			}
		}
		
		if (id == -1) {
			Log.logger().info("Max number of applications reached");
			throw new MaxGlobalNumberOfApplicationsReached(name);
		}
		
		return id;
	}
	
	private void removeApplication(Application application) {
		
		// Remove the application from the port manager.
		HashSet<Integer> ports = PortManager.getInstance().removeApplication(application.getId());
		
		// Iterate on the ports of the application.
		String listOfPorts = "";
		
		for (Integer p : ports) {
			listOfPorts += " " + p;
		}
		
		if (!ports.isEmpty()) {
			Log.logger().fine("Application " + application.getNameId() + " has released ports" + listOfPorts);
		}
		else {
			Log.logger().fine("Application " + application.getNameId() + " has no ports to release");
		}
		
		// Remove the application from the map.
		applicationMap.remove(application.getId());
	}
	
	/**
	 * start application
	 * 
	 * @param commandArray
	 * @param args
	 * @param starterProxyPort 
	 * @param starterLinked 
	 * @param serverEndpoint 
	 * @param result 
	 * @return
	 * @throws UnknownApplicationException
	 * @throws MaxNumberOfApplicationsReached
	 * @throws ApplicationAlreadyExecuting
	 */
	public synchronized Application startApplication(String name, String[] args, ApplicationIdentity starter, int starterProxyPort, boolean starterLinked) throws UnknownApplicationException, MaxNumberOfApplicationsReached, MaxGlobalNumberOfApplicationsReached {
		
		ApplicationConfig config = this.verifyApplicationExistence(name);
		Log.logger().fine("Trying to start " + name);

		// Verify if the application is already running.
		verifyNumberOfInstances(config.getName(), config.runMaxApplications());

		// Find an id, throws an exception if there is no id available.
		int id = findId(name);
		
		// Create the application. The proxy host endpoint is passed.
		Application application = new RegisteredApplication(ConfigManager.getInstance().getHostEndpoint(), id, config, args, starter, starterProxyPort, starterLinked);
		applicationMap.put(id, application);
		
		// Threads.
		// Create the lifecyle application thread.
		LifecycleApplicationThread lifecycleThread = new LifecycleApplicationThread(application, this, Log.logger());
		lifecycleThread.start();
		
		// Create the stream thread.
		if (application.isWritingStream() || application.hasOutputStream()) {
			if (application.getLogPath() != null) {
				Log.logger().fine("Application " + application.getNameId() + " has stream written to log file '" + application.getLogPath() + "'");
			}
			else {
				Log.logger().fine("Application " + application.getNameId() + " has stream");
			}

			// The thread is built but not started here because it requires that the process is started.
			StreamApplicationThread streamThread = new StreamApplicationThread(application, this);
			application.setStreamThread(streamThread);
		}
		
		return application;
	}


	public synchronized void setApplicationStopHandler(int id, int stoppingTime) throws IdNotFoundException {
		
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		application.setStopHandler(stoppingTime);
	}

	
	/**
	 * stop application
	 * 
	 * @param id
	 * @param i 
	 * @throws IdNotFoundException
	 */
	public synchronized String stopApplication(int id, boolean link) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			
			String name = application.getName();
			
			// If the process is dead, there is no thread.
			if (application.getProcessState().equals(ProcessState.DEAD)) {
				removeApplication(application);
			}
			else {
				// The following call will have no effect if it was already called.
				application.setHasToStop(true, false, link);
			}
			return name;
		}
		else {
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
			
			// If process is dead, remove the application.
			if (application.getProcessState().equals(ProcessState.DEAD)) {
				removeApplication(application);
			}
			else {
				application.setHasToStop(true, true, false);
			}
			return name;
		}
		else {
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
				
				// If process is dead, remove the application.
				if (application.getProcessState().equals(ProcessState.DEAD)) {
					removeApplication(application);
				}
				else {
					application.setHasToStop(true, true, false);
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
	public synchronized LinkedList<ApplicationInfo> getApplicationInfos() {
		
		LinkedList<ApplicationInfo> list = new LinkedList<ApplicationInfo>();
		
		String args = null;

		for (java.util.Map.Entry<Integer, Application> entry : applicationMap.entrySet()) {
			Application application = entry.getValue();
			if (application.getArgs() == null) {
				args = "";
			}
			else {
				args = String.join(" ", application.getArgs());
			}
			
			ApplicationInfo applicationInfo = new ApplicationInfo(application.getId(),
												application.getPid(),
												application.getApplicationState(), 
												application.getPastApplicationStates(),
												args, 
												application.hasToStop(), 
												application.hasOutputStream(), 
												application.isWritingStream(), 
												application.runSingle(), 
												application.isRestart(), 
												application.hasInfoArg(), 
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
		
		// Find the application.
		if (applicationMap.containsKey(id)) {
			Application application = applicationMap.get(id);
						
			Log.logger().fine("Application " + application.getNameId() + " has stream port " + application.getOutputStreamPort());
			
			return application.getOutputStreamPort();
		}
		else {
			throw new IdNotFoundException();
		}

	}
	
	/**
	 * verify if an application already run
	 * 
	 * @param config
	 * @throws ApplicationAlreadyExecuting
	 * @throws MaxNumberOfApplicationsReached 
	 * @throws MaxGlobalNumberOfApplicationsReached 
	 */
	private void verifyNumberOfInstances(String name, int maxNumber) throws MaxNumberOfApplicationsReached, MaxGlobalNumberOfApplicationsReached {
		
		// Verify the global number of running apps.
		if (applicationMap.size() == ConfigManager.getInstance().getMaxNumberOfApplications()) {
			Log.logger().info("Global max number of running applications reached");
			
			throw new MaxGlobalNumberOfApplicationsReached(name);
		}
		
		// Count the application instances.
		int counter = 0;
		
		// Check the name.
		for (java.util.Map.Entry<Integer, Application> entry : applicationMap.entrySet()) {
		
			Application application = entry.getValue();
			
			if (application.getName().equals(name)) {
				
				// Check if the process is dead.
				if (application.getProcessState() == ProcessState.DEAD) {
					removeApplication(application);
				}
				else {
					// Increment the counter.
					++counter;
					
					if (maxNumber != -1 && counter >= maxNumber) {
						Log.logger().info("Max number of running applications (" + maxNumber + ") reached for application " + name);
						
						throw new MaxNumberOfApplicationsReached(application.getName());
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
		}
		else {
			return false;
		}
	}

	/**
	 * send parameters to an application
	 * 
	 * @param id
	 * @param inputs
	 * @throws IdNotFoundException
	 * @throws UnregisteredApplicationException 
	 */
	public synchronized void writeToInputStream(int id, String[] inputs) throws IdNotFoundException, UnregisteredApplicationException {
		
		if (applicationMap.containsKey(id)) {
			Application application = applicationMap.get(id);
			
			// The process can be null in case it is an unregistered application.
			if (application.getProcess() == null) {
				// We should throw a specific exception.
				throw new UnregisteredApplicationException();
			}
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(application.getProcess().getOutputStream()));
			
			// Build simple string from parameters.
			String inputString = "";
			for (int i = 0; i < inputs.length - 1; i++) {
				inputString += inputs[i] + " ";
			}
			inputString += inputs[inputs.length - 1];
			
			// Send the parameters string.
			try {
				writer.write(inputString);
				writer.flush();
			}
			catch (IOException e) {
				Log.logger().severe("Unable to write to input for application " + application.getNameId());
				try {
					writer.close();
				}
				catch (IOException ec) {
					// Do nothing.
				}
			}
		}
		else {
			throw new IdNotFoundException();
		}
	}
	
	public synchronized void setApplicationState(Application application, int applicationState, int exitValue) {
		
		// States are : NIL, STARTING, RUNNING, STOPPING, KILLING, PROCESSING_FAILURE, FAILURE, SUCCESS, STOPPED, KILLED.
		// Set the status of the application
		application.setState(applicationState);
		
		// Send the status.
		sendStatus(application.getId(), application.getName(), applicationState, application.getPastApplicationStates(), exitValue);
				
		// Remove the application for terminal states.
		if (applicationState == ApplicationState.FAILURE
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
		// Process is alive.
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
		
		// States are: NIL, STARTING, RUNNING, STOPPING, KILLING, PROCESSING_FAILURE, FAILURE, SUCCESS, STOPPED, KILLED.
		// State that can be set by the client : RUNNING
		if (state == ApplicationState.RUNNING) {
			// current state can only be STARTING
			if (currentState == ApplicationState.STARTING) {
				setApplicationState(application, state);
				return true;
			}
			else if (currentState == ApplicationState.RUNNING) {
				// Do not change the state, but it is ok.
				return true;
			}
			else {
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

		// Send the result that is not stored.
		sendResult(application.getId(), application.getName(), data);
		
		return true;
	}
	
	public synchronized StatusInfo getApplicationState(int id) {
		
		StatusInfo status = new StatusInfo();
		status.setId(id);
		
		if (!applicationMap.containsKey(id)) {
			status.setName("?");
			status.setApplicationState(ApplicationState.NIL);
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

	public int newStartedUnregisteredApplication(String name, long pid) throws MaxNumberOfApplicationsReached, MaxGlobalNumberOfApplicationsReached {
		
		// Verify if the application is already running.
		verifyNumberOfInstances(name, -1);
		
		// Find an id, throws an exception if there is no id available.
		int id = findId(name);
		
		// Create the application.
		//Application application = new UnregisteredApplication(ConfigManager.getInstance().getResponderProxyHostEndpoint(), id, name, pid);
		Application application = new UnregisteredApplication(ConfigManager.getInstance().getHostEndpoint(), id, name, pid);
		applicationMap.put(id, application);
		
		// Threads.
		// Create the lifecycle application thread.
		LifecycleApplicationThread verifyThread = new LifecycleApplicationThread(application, this, Log.logger());
		verifyThread.start();
		
		Log.logger().fine("Application " + application.getNameId() + " is started");
		
		// No stream thread.
		return id;
	}
	
	public int newStartedUnregisteredApplication(String name) throws MaxNumberOfApplicationsReached, MaxGlobalNumberOfApplicationsReached {
		return newStartedUnregisteredApplication(name, 0);
	}

	public String setUnregisteredApplicationTerminated(int id) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			
			String name = application.getName();
			
			// Terminate the application that is unregistered.
			((UnregisteredApplication)application).terminate();
			
			// Remove the application.
			removeApplication(application);
			
			Log.logger().info("Application " + application.getNameId() + " is terminated");
			
			return name;
		}
		else {
			throw new IdNotFoundException();
		}
	}

	public void storeKeyValue(int id, String key, String value) throws IdNotFoundException, KeyAlreadyExistsException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			if (!application.storeKeyValue(key, value)) {
				throw new KeyAlreadyExistsException();
			}
			
			// Send the event.
			sendStoreKeyValue(id, application.getName(), key, value);
		}
		else {
			throw new IdNotFoundException();
		}
	}

	public String getKeyValue(int id, String key) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			return application.getKeyValue(key);
		}
		else {
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
		}
		else {
			throw new IdNotFoundException();
		}
	}

	public int requestPort(int id) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			int port = PortManager.getInstance().requestPort(application.getName(), id);

			Log.logger().fine("Application " + application.getNameId() + " has port " + port);
			
			return port;
		}
		else {
			throw new IdNotFoundException();
		}
	}

	public void setPortUnavailable(int id, int port) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			PortManager.getInstance().setPortUnavailable(port);
			
			Log.logger().fine("Application " + application.getNameId() + " has set port " + port + " unavailable");
		}
		else {
			throw new IdNotFoundException();
		}
	}
	
	public void releasePort(int id, int port) throws IdNotFoundException {
		
		Application application = applicationMap.get(id);
		
		if (application != null) {
			boolean removed = PortManager.getInstance().removePort(port);
			
			Log.logger().fine("Application " + application.getNameId() + " has released port " + port);
		}
		else {
			throw new IdNotFoundException();
		}
	}
	
	public List<PortInfo> getPortList() {
		
		LinkedList<PortInfo> result = new LinkedList<PortInfo>();
		
		for (Entry<Integer, PortManager.State> p : PortManager.getInstance().getReservedPorts().entrySet()) {

			int port = p.getKey();
			String status = p.getValue().status.toString();
			String application = "";
			
			String applicationName = p.getValue().applicationName;
			Integer applicationId = p.getValue().applicationId;
			
			if (applicationName != null) {
				application = applicationName;
				
				if (applicationId != null) {
					application += "." + applicationId;
				}
			}
			
			result.add(new PortInfo(port, status, application));
		}
		
		return result;
	}
}