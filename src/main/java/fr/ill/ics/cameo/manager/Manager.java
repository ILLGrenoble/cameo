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

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.google.protobuf.ByteString;

import fr.ill.ics.cameo.exception.ApplicationAlreadyRunning;
import fr.ill.ics.cameo.exception.IdNotFoundException;
import fr.ill.ics.cameo.exception.MaxNumberOfApplicationsReached;
import fr.ill.ics.cameo.exception.StreamNotPublishedException;
import fr.ill.ics.cameo.exception.UnknownApplicationException;
import fr.ill.ics.cameo.exception.UnknownPublisherException;
import fr.ill.ics.cameo.proto.Messages.PortEvent;
import fr.ill.ics.cameo.proto.Messages.PublisherEvent;
import fr.ill.ics.cameo.proto.Messages.ResultEvent;
import fr.ill.ics.cameo.proto.Messages.StatusEvent;
import fr.ill.ics.cameo.threads.StreamApplicationThread;
import fr.ill.ics.cameo.threads.VerifyApplicationThread;

public class Manager extends ConfigLoader {

	private ConcurrentHashMap<Integer, Application> applicationMap;
	private static int MAX_ID = 65536; 
	private int maxId = 0;
	private Socket eventPublisher;
	private HashMap<String, Socket> streamPublishers = new HashMap<String, Socket>();

	private final static String PUBLISHER_PREFIX = "pub.";
	private final static String SYNCHRONIZER_PREFIX = "sync.";
	private final static String RESPONDER_PREFIX = "rep.";
	private final static String REQUESTER_PREFIX = "req.";
	
	public Manager(String xmlPath) {
		super(xmlPath);
		LogInfo.getInstance().init();
		LogInfo.getInstance().getLogger().fine("Endpoint is " + ConfigManager.getInstance().getHostEndpoint());
		
		showApplicationConfigs();
		
		applicationMap = new ConcurrentHashMap<Integer, Application>();
		
		// security test
		if (ConfigManager.getInstance().getMaxNumberOfApplications() > MAX_ID) {
			MAX_ID = ConfigManager.getInstance().getMaxNumberOfApplications();
		}
		
		LogInfo.getInstance().getLogger().fine("Max Id is " + MAX_ID);
	}

	public Manager(InputStream configStream) {
		super(configStream);
		LogInfo.getInstance().init();
		LogInfo.getInstance().getLogger().fine("Endpoint is " + ConfigManager.getInstance().getHostEndpoint());
		
		showApplicationConfigs();
		
		applicationMap = new ConcurrentHashMap<Integer, Application>();
		
		// security test
		if (ConfigManager.getInstance().getMaxNumberOfApplications() > MAX_ID) {
			MAX_ID = ConfigManager.getInstance().getMaxNumberOfApplications();
		}
		
		LogInfo.getInstance().getLogger().fine("Max Id is " + MAX_ID);
	}

	public synchronized void initStreamSockets(ZContext context) {
		
		eventPublisher = context.createSocket(ZMQ.PUB);
		
		int port = ConfigManager.getInstance().getStreamPort();
		eventPublisher.bind("tcp://*:" + port);
		
		LogInfo.getInstance().getLogger().info("Status socket on port " + port);
		
		// iterate the application configurations
		for (ApplicationConfig c : applicationSet) {
			Socket streamPublisher = context.createSocket(ZMQ.PUB);
			if (c.hasStream()) {
				port = c.getStreamPort();
				streamPublisher.bind("tcp://*:" + port);
				
				streamPublishers.put(c.getName(), streamPublisher);
				
				LogInfo.getInstance().getLogger().info("Application " + c.getName() + " output socket on port " + port);
			}	
		}
	}

	public Socket getStreamPublisher(String name) {
		return streamPublishers.get(name);
	}

	public synchronized void sendStatus(int id, String name, int state, int pastStates) {
		
		StatusEvent event = StatusEvent.newBuilder()
										.setId(id)
										.setName(name)
										.setApplicationState(state)
										.setPastApplicationStates(pastStates)
										.build();
			
		eventPublisher.sendMore("STATUS");
		eventPublisher.send(event.toByteArray(), 0);
	}

	public synchronized void sendResult(int id, String name, ByteString data) {
		
		ResultEvent event = ResultEvent.newBuilder()
				.setId(id)
				.setName(name)
				.setData(data)
				.build();

		eventPublisher.sendMore("RESULT");
		eventPublisher.send(event.toByteArray(), 0);
	}
	
	public synchronized void sendPublisher(int id, String name, String publisherName) {
		
		PublisherEvent event = PublisherEvent.newBuilder()
										.setId(id)
										.setName(name)
										.setPublisherName(publisherName)
										.build();
				
		eventPublisher.sendMore("PUBLISHER");
		eventPublisher.send(event.toByteArray(), 0);
	}
	
	public synchronized void sendPort(int id, String name, String portName) {
		
		PortEvent event = PortEvent.newBuilder()
										.setId(id)
										.setName(name)
										.setPortName(portName)
										.build();
				
		eventPublisher.sendMore("PORT");
		eventPublisher.send(event.toByteArray(), 0);
	}
	
	private int findFreeId(int begin, int end) {
		
		for (int i = begin; i < end; i++) {
			if (!applicationMap.containsKey(i)) {
				return i;
			}
		}
		
		return -1;
	}
	
	private void removeApplication(Application application) {
		
		// Iterate on the ports of the application to remove them.
		HashMap<String, Integer> ports = application.getPorts();
		for (Entry<String, Integer> e : ports.entrySet()) {
			ConfigManager.getInstance().removePort(e.getValue());	
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
	 * @throws ApplicationAlreadyRunning
	 */
	public synchronized Application startApplication(String name, String[] args, String starterReference) throws UnknownApplicationException, MaxNumberOfApplicationsReached, ApplicationAlreadyRunning {
		
		ApplicationConfig applicationConfig = this.verifyApplicationExistence(name);
		LogInfo.getInstance().getLogger().fine("Trying to start " + name);

		// Verify if the application is already running and if run = single
		if (applicationConfig.runsSingle()) {
			verifyNumberOfInstance(applicationConfig);
		}

		if (applicationMap.size() == ConfigManager.getInstance().getMaxNumberOfApplications()) {
			
			LogInfo.getInstance().getLogger().info("Max number of applications reached");
			throw new MaxNumberOfApplicationsReached();
		}
		
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
			LogInfo.getInstance().getLogger().info("Max number of applications reached");
			throw new MaxNumberOfApplicationsReached();
		}
		
		// Create application
		Application application = new Application(ConfigManager.getInstance().getHostEndpoint(), id, applicationConfig, args, starterReference);
		applicationMap.put(id, application);
		
		// Threads
		// Verifiy application thread
		VerifyApplicationThread verifyThread = new VerifyApplicationThread(application, this, LogInfo.getInstance().getLogger());
		verifyThread.start();
		
		LogInfo.getInstance().getLogger().fine("Application " + application.getNameId() + " write stream = " + application.isWriteStream() + ", show stream = " + application.hasStream());
		
		// Stream thread
		if (application.isWriteStream() || application.hasStream()) {
			if (application.getLogPath() != null) {
				LogInfo.getInstance().getLogger().info("Set up stream for application " + application.getNameId() + " with log file '" + application.getLogPath() + "'");
			} else {
				LogInfo.getInstance().getLogger().info("Set up stream for application " + application.getNameId());
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
			LogInfo.getInstance().getLogger().info("Application " + application.getNameId() + " is stopping...");
			
			return name;
			
		} else {
			LogInfo.getInstance().getLogger().info("Application with id " + id + " doesn't exist");
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
			LogInfo.getInstance().getLogger().info("Killing application " + application.getNameId());
			
			return name;
			
		} else {
			LogInfo.getInstance().getLogger().info("Application with id " + id + " doesn't exist");
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
				LogInfo.getInstance().getLogger().info("Killing application " + application.getNameId());
				application.kill();
				LogInfo.getInstance().getLogger().info("Killed application " + application.getNameId());
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
		LogInfo.getInstance().getLogger().fine("Showing application");
		String args = null;

		for (java.util.Map.Entry<Integer, Application> entry : applicationMap.entrySet()) {
			Application application = entry.getValue();
			if (application.getArgs() == null) {
				args = "no arguments";
			} else {
				args = String.join(" ", application.getArgs());
			}
			
			ApplicationInfo applicationInfo = new ApplicationInfo(application.getId(), application.getApplicationState(), application.getPastApplicationStates(), application.getProcessState(), args, application.hasToStop(), application.hasStream(), application.isWriteStream(), application.runsSingle(), application.isRestart(), application.isPassInfo(), application.getName(), application.getStartExecutable(), application.getStartingTime(), application.getRetries(), application.getLogPath(), application.getStoppingTime(), application.getStopExecutable());
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
	public synchronized int showStream(int id) throws IdNotFoundException, UnknownApplicationException, StreamNotPublishedException {
		
		// find application
		if (applicationMap.containsKey(id)) {
			Application application = applicationMap.get(id);
						
			LogInfo.getInstance().getLogger().fine("Application " + application.getNameId() + " has stream port " + application.getStreamPort());
			
			return application.getStreamPort();
			
		} else {
			LogInfo.getInstance().getLogger().info("Application with id " + id + " doesn't exist");
			throw new IdNotFoundException();
		}

	}
	
	/**
	 * verify if an application already run
	 * 
	 * @param instanceOfApplication
	 * @throws ApplicationAlreadyRunning
	 */
	private void verifyNumberOfInstance(ApplicationConfig instanceOfApplication) throws ApplicationAlreadyRunning {
		// just check name
		for (java.util.Map.Entry<Integer, Application> entry : applicationMap.entrySet()) {
			Application value = entry.getValue();
			if (value.getName().equalsIgnoreCase(instanceOfApplication.getName())) {
				// check if application is dead
				if (value.getProcessState() == ProcessState.DEAD) {
					removeApplication(value);
				} else {
					LogInfo.getInstance().getLogger().fine("The application is already running");
					LogInfo.getInstance().getLogger().info("Application with name " + value.getName() + " is already running with id " + value.getId());
					throw new ApplicationAlreadyRunning();
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
				|| state == ApplicationState.RUNNING) {
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
	 */
	public synchronized void writeToInputStream(int id, String[] parametersToSend) throws IdNotFoundException {
		
		if (applicationMap.containsKey(id)) {
			Application application = applicationMap.get(id);
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
				LogInfo.getInstance().getLogger().severe("Enable to send parameters to application " + application.getNameId());
				try {
					writer.close();
				} catch (IOException ec) {
					// do nothing
				}
			}
		
		} else {
			LogInfo.getInstance().getLogger().info("Application with id " + id + " doesn't exist");
			throw new IdNotFoundException();
		}
	}
			
	/**
	 * The set application process is synchronized to ensure that the manager can access the value safely.
	 * @param application
	 * @param process
	 */
	public synchronized void setApplicationProcess(Application application, Process process) {
		application.setProcess(process);
	}

	public synchronized void setApplicationState(Application application, int applicationState) {
		
		// states are : UNKNOWN, STARTING, RUNNING, STOPPING, KILLING, PROCESSING_ERROR, ERROR, SUCCESS, STOPPED, KILLED
		// set the status of the application
		application.setState(applicationState);
		
		// send the status
		sendStatus(application.getId(), application.getName(), applicationState, application.getPastApplicationStates());
				
		// remove the application for terminal states
		if (applicationState == ApplicationState.ERROR
			|| applicationState == ApplicationState.STOPPED
			|| applicationState == ApplicationState.KILLED
			|| applicationState == ApplicationState.SUCCESS) {
			
			removeApplication(application);
		}
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
	

	public synchronized boolean setApplicationResult(int id, ByteString data) throws IdNotFoundException {
	
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);

		// send the result that is not stored
		sendResult(application.getId(), application.getName(), data);
		
		return true;
	}
	
	public synchronized StatusEvent getApplicationState(int id) throws IdNotFoundException {
		
		if (!applicationMap.containsKey(id)) {
			throw new IdNotFoundException();
		}
		
		Application application = applicationMap.get(id);
		StatusEvent status = StatusEvent.newBuilder()
				.setId(id)
				.setName(application.getName())
				.setApplicationState(application.getApplicationState())
				.setPastApplicationStates(application.getPastApplicationStates())
				.setPastApplicationStates(0)
				.build();
		
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
		
		int port = ConfigManager.getInstance().getNextPort();
		ports.put(portName, port);

		sendPort(id, application.getName(), portName);
		
		LogInfo.getInstance().getLogger().info("Application " + application.getNameId() + " socket " + portName + " on port " + port);
		
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
			ConfigManager.getInstance().removePort(ports.get(portName));
			ports.remove(portName);
			LogInfo.getInstance().getLogger().info("Application " + application.getNameId() + " removed socket " + portName);
			
			return true;
		}

		LogInfo.getInstance().getLogger().info("Application " + application.getNameId() + " cannot remove socket " + portName);
		
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
		int publisherPort = ConfigManager.getInstance().getNextPort();
		int synchronizerPort = ConfigManager.getInstance().getNextPort();

		HashMap<String, Integer> ports = application.getPorts();
		
		ports.put(PUBLISHER_PREFIX + publisherName, publisherPort);
		ports.put(SYNCHRONIZER_PREFIX + publisherName, synchronizerPort);
		
		Application.Publisher publisher = new Application.Publisher();
		publisher.numberOfSubscribers = numberOfSubscribers;
		
		publishers.put(publisherName, publisher);

		LogInfo.getInstance().getLogger().info("Application " + application.getNameId() + " publisher socket on ports " + publisherPort + " and " + synchronizerPort);

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
			
			ConfigManager.getInstance().removePort(publisherPort);
			ConfigManager.getInstance().removePort(synchronizerPort);
			publishers.remove(publisherName);
			
			LogInfo.getInstance().getLogger().info("Application " + application.getNameId() + " closes publisher socket " + publisherName + " on ports " + publisherPort + " and " + synchronizerPort);
			
			return true;
		}
		
		LogInfo.getInstance().getLogger().info("Application " + application.getNameId() + " cannot close publisher socket " + publisherName);
		
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
}