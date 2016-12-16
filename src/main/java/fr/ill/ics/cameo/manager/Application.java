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

import fr.ill.ics.cameo.server.Converter;
import fr.ill.ics.cameo.threads.StreamApplicationThread;

/**
 * describe an application
 */
public abstract class Application extends ApplicationConfig {

	protected String endpoint;
	protected int id;
	
	protected int applicationState = ApplicationState.UNKNOWN;
	protected int pastApplicationStates = ApplicationState.UNKNOWN;
	protected ProcessState processState = ProcessState.UNKNOWN;
	protected boolean hasToStop = false;
	protected boolean hasToStopImmediately = false;
	protected StreamApplicationThread streamThread = null;
	protected boolean streamThreadStarted = false;
	
	public static class Publisher {
		public int numberOfSubscribers;
	}
		
	private HashMap<String, Integer> ports = new HashMap<String, Integer>();
	private HashMap<String, Publisher> publishers = new HashMap<String, Publisher>();
	private HashSet<String> responders = new HashSet<String>();
	
	public Application(String endpoint, int id) {
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
	 * The application is killed if a kill is requested or stopping time is 0.
	 * @return
	 */
	synchronized public boolean hasToBeKilled() {
		return hasToStopImmediately || (stoppingTime == 0);
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
	// Synchronized set methods
	
	synchronized void setState(int applicationState) {
		if (applicationState != this.applicationState) {
			LogInfo.getInstance().getLogger().fine("Application " + getNameId() + " sets application state " + Converter.toString(applicationState));
			this.applicationState = applicationState;
			this.pastApplicationStates |= applicationState;
		}	
	}

	synchronized void setProcessState(ProcessState processState) {
		if (processState != this.processState) {
			LogInfo.getInstance().getLogger().fine("Application " + getNameId() + " sets process state " + processState);
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
	 * Launches the error executable.
	 * It is not synchronized as it is blocking.
	 * Arguments passed are id, error code
	 */
	public void executeError(int errorCode, int currentState) {
		
		if (errorExecutable == null) {
			return;
		}
		
		LogInfo.getInstance().getLogger().fine("Launching error executable for application " + this.getNameId());
		
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
		commandList.add(new Integer(this.getId()).toString());
		commandList.add(new Integer(errorCode).toString());
		commandList.add(Converter.toString(currentState));
		
		// Prepare the command
		String command[] = new String[commandList.size()];
		commandList.toArray(command);
	
		String commandString = String.join(" ", command);
		LogInfo.getInstance().getLogger().fine("Start " + commandString);
		
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
			LogInfo.getInstance().getLogger().fine("Processed error in " + (end.getTime() - begin.getTime()) + "ms");
			
		} catch (IOException e) {
			LogInfo.getInstance().getLogger().severe("Error process is not launched for application " + this.getNameId() + " : " + e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			
		}
		LogInfo.getInstance().getLogger().info("Finished processing error for application " + this.getNameId());
	}
	
	abstract public void executeStop(String PID);
	abstract public void kill();
	abstract public void reset();
	
	/**
	 * @override
	 */
	public String toString() {
		return super.toString() + "\napplicationState=" + Converter.toString(this.applicationState) + "\nprocessState=" + this.processState + "\nhasToStop=" + this.hasToStop() + "\nshowStream=" + this.hasStream() + "\nstreamPort=" + this.getStreamPort() + "\nwriteStream=" + this.isWriteStream() + "\nid=" + this.getId();
	}

}