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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import fr.ill.ics.cameo.ProcessHandlerImpl;
import fr.ill.ics.cameo.server.Converter;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.threads.StreamApplicationThread;

/**
 * describe an application
 */
public abstract class Application extends ApplicationConfig {

	protected Endpoint endpoint;
	protected int id;
	protected ProcessHandlerImpl processHandle;
	
	protected int applicationState = ApplicationState.UNKNOWN;
	protected int pastApplicationStates = ApplicationState.UNKNOWN;
	protected ProcessState processState = ProcessState.UNKNOWN;
	protected boolean hasStopHandler = false;
	protected boolean hasToStop = false;
	protected boolean hasToStopImmediately = false;
	protected StreamApplicationThread streamThread = null;
	protected boolean streamThreadStarted = false;
	private HashMap<String, String> keyValues = new HashMap<String, String>();
	
	public static class Publisher {
		public int numberOfSubscribers;
	}
		
	private HashMap<String, Integer> ports = new HashMap<String, Integer>();
	private HashMap<String, Publisher> publishers = new HashMap<String, Publisher>();
	private HashSet<String> responders = new HashSet<String>();
	
	public Application(Endpoint endpoint, int id) {
		super();
		
		this.endpoint = endpoint;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getNameId() {
		return name + "." + id;
	}

	abstract public String[] getArgs();
	
	public long getPid() {
		// In case the application does not provide a pid.
		if (processHandle == null) {
			return 0;
		}
		return processHandle.getPid();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// Synchronized get methods
	
	synchronized public boolean isWriteStream() {
		return (logPath != null);
	}
	
	abstract public boolean isManaged();
	abstract public Process getProcess();
	abstract public boolean isAlive();
	
	/**
	 * Returns true if duration 
	 * @param duration
	 * @return
	 */
	synchronized public boolean isTimeout(double duration) {
		if (stoppingTime < 0) {
			return false;
		}
		
		return (duration > stoppingTime);
	}
		
	synchronized public int getApplicationState() {
		return applicationState;
	}
	
	synchronized public int getPastApplicationStates() {
		return pastApplicationStates;
	}

	synchronized public ProcessState getProcessState() {
		return processState;
	}
	
	synchronized public boolean hasToStop() {
		return hasToStop;
	}

	/**
	 * The application is killed if a kill is requested or it has no stop handler or stopping time is 0.
	 * @return
	 */
	synchronized public boolean hasToBeKilled() {
		return hasToStopImmediately
			|| !hasStopHandler
			|| (stoppingTime == 0);
	}
	
	synchronized public StreamApplicationThread getStreamThread() {
		return this.streamThread;
	}
		
	synchronized public HashMap<String, Publisher> getPublishers() {
		return publishers;
	}
	
	synchronized public HashSet<String> getResponders() {
		return responders;
	}

	synchronized public HashMap<String, Integer> getPorts() {
		return ports;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Storage methods
	
	public void storeKeyValue(String key, String value) {
		keyValues.put(key, value);
	}
	
	public String getKeyValue(String key) {
		return keyValues.get(key);
	}
	
	public boolean removeKey(String key) {
		return (keyValues.remove(key) != null);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// Synchronized set methods
	
	synchronized public void setStopHandler(int stoppingTime) {
		this.hasStopHandler = true;
		
		// Stopping is overriden if it has been defined in the code.
		if (stoppingTime > -1) {
			this.stoppingTime = stoppingTime;
		}
		
		Log.logger().fine("Application " + getNameId() + " has a stop handler with stopping time " + this.stoppingTime + "s");
	}
	
	synchronized void setState(int applicationState) {
		if (applicationState != this.applicationState) {
			Log.logger().fine("Application " + getNameId() + " has state " + Converter.toString(applicationState));
			this.applicationState = applicationState;
			this.pastApplicationStates |= applicationState;
		}	
	}

	synchronized void setProcessState(ProcessState processState) {
		if (processState != this.processState) {
			this.processState = processState;
		}	
	}
	
	synchronized void setHasToStop(boolean hasToStop, boolean immediately) {
		this.hasToStop = hasToStop;
		this.hasToStopImmediately = immediately;
	}
	
	synchronized void setStreamThread(StreamApplicationThread thread) {
		this.streamThread = thread;
		this.streamThreadStarted = false;
	}
	
	synchronized public void startStreamThread() {
		if (streamThread != null && !streamThreadStarted) {
			streamThreadStarted = true;
			streamThread.start();
		}
	}
	
	synchronized public void sendEndOfStream() {
		if (streamThread != null && !streamThreadStarted) {
			streamThread.sendEndOfStream();
		}
	}
			
	abstract public void start();
	
	/**
	 * Create the string from the command list. The first element is the program name, the following are the arguments for which " are added around.
	 * @param command
	 * @return
	 */
	protected String commandListToString(String[] command) {
		StringBuilder builder = new StringBuilder("$ " + command[0]);
		
		for (int i = 1; i < command.length; ++i) {
			builder.append(" \"" + command[i] + "\"");
		}
		
		return builder.toString();
	}
	
	/**
	 * Launches the error executable.
	 * It is not synchronized as it is blocking.
	 * Arguments passed are id, error code
	 */
	public void executeError(int errorCode, int currentState) {
		
		if (errorExecutable == null) {
			return;
		}
		
		Log.logger().fine("Application " + this.getNameId() + " is launching error executable");
		
		// Build the command arguments
		ArrayList<String> commandList = new ArrayList<String>();
		commandList.add(errorExecutable);
		
		if (errorArgs != null) {
			for (int i = 0; i < errorArgs.length; i++) {
				commandList.add(errorArgs[i]);
				i++;
			}
		}

		// Add the special error arguments
		commandList.add(this.getId() + "");
		commandList.add(errorCode + "");
		commandList.add(Converter.toString(currentState));
		
		// Prepare the command
		String command[] = new String[commandList.size()];
		commandList.toArray(command);
	
		Log.logger().info("Application " + this.getNameId() + " executes error process " + commandListToString(command));
		
		ProcessBuilder builder = new ProcessBuilder(command);
		
		// set the working directory
		if (getDirectory() != null) {
			builder.directory(new File(getDirectory()));
		}	
		
		try {
			Date begin = new Date();
			
			Process process = builder.start();
			process.waitFor();
			
			Date end = new Date();
			Log.logger().fine("Application " + this.getNameId() + " has error processed executed in " + (end.getTime() - begin.getTime()) + "ms");
			
		} catch (IOException e) {
			Log.logger().severe("Application " + this.getNameId() + " has not executed error process : " + e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			
		}
		Log.logger().info("Application " + this.getNameId() + " has error process finished");
	}
	
	abstract public void executeStop();
	abstract public void kill();
	abstract public void reset();

	public void waitFor() {
		// The process handle can be null if the application does not provide a pid.
		if (processHandle != null) {
			processHandle.waitFor();
		}
	}
	
	/**
	 * @override
	 */
	public String toString() {
		return super.toString() + "\napplicationState=" + Converter.toString(this.applicationState) + "\nprocessState=" + this.processState + "\nhasToStop=" + this.hasToStop() + "\nshowStream=" + this.hasStream() + "\nstreamPort=" + this.getStreamPort() + "\nwriteStream=" + this.isWriteStream() + "\nid=" + this.getId();
	}

}