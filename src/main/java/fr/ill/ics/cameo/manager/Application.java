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
import java.util.Map;

import fr.ill.ics.cameo.server.Converter;
import fr.ill.ics.cameo.threads.StreamApplicationThread;

/**
 * describe an application
 */
public class Application extends ApplicationConfig {

	private String endpoint;
	private String starterReference;
	private int id;
	private String[] args;
	private java.lang.Process process;
	private int applicationState = ApplicationState.UNKNOWN;
	private int pastApplicationStates = ApplicationState.UNKNOWN;
	private ProcessState processState = ProcessState.UNKNOWN;
	private boolean hasToStop = false;
	private boolean hasToStopImmediately = false;
	private StreamApplicationThread streamThread = null;
	private boolean streamThreadStarted = false;
	
	public static class Publisher {
		public int numberOfSubscribers;
	}
		
	private HashMap<String, Integer> ports = new HashMap<String, Integer>();
	private HashMap<String, Publisher> publishers = new HashMap<String, Publisher>();
	private HashSet<String> responders = new HashSet<String>();
	
	public Application(String endpoint, int id, ApplicationConfig applicationConfig, String[] args, String starterReference) {
		super(applicationConfig);
		
		this.endpoint = endpoint;
		this.starterReference = starterReference;
		this.id = id;
		this.args = args;
	}

	public int getId() {
		return id;
	}

	public String getNameId() {
		return name + "." + id;
	}

	public String[] getArgs() {
		return args;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// Synchronized get methods
	
	synchronized public boolean isWriteStream() {
		return (logPath != null);
	}
	
	synchronized public Process getProcess() {
		return process;
	}

	synchronized public boolean isAlive() {
		if (process != null) {
			return process.isAlive();
		}
		return false;
	}
	
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
	
	synchronized void setProcess(Process process) {
		this.process = process;
	}
	
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
		// Return immediately if the application is already stopping.
		if (this.hasToStop) {
			return;
		}
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
		
	/**
	 * The method is synchronized as it is not blocking.
	 */
	public synchronized void start() {
		
		try {
			// Build the command arguments
			ArrayList<String> commandList = new ArrayList<String>();
			
			// First add the executable
			commandList.add(getStartExecutable());
			
			// Add the args
			if (startArgs != null) {
				for (int i = 0; i < startArgs.length; i++) {
					commandList.add(startArgs[i]);
				}
			}
			
			// Add the additional args
			if (args != null) {
				for (int i = 0; i < args.length; i++) {
					commandList.add(args[i]);
				}
			}
			
			// Add the endpoint and id
			if (isPassInfo()) {
				commandList.add(endpoint + ":" + name + "." + Integer.toString(getId()) + ":" + starterReference);
			}
			
			// Prepare the command
			String command[] = new String[commandList.size()];
			commandList.toArray(command);

			String commandString = String.join(" ", command);
			LogInfo.getInstance().getLogger().fine("Start " + commandString);
			
			// Create the builder from the command
			ProcessBuilder builder = new ProcessBuilder(command);
			
			// Set the working directory
			if (getDirectory() != null) {
				builder.directory(new File(getDirectory()));
			}	
			
			// Standard output and error output are merged
			builder.redirectErrorStream(true); 
			
			// Workaround for Mac OS
			// Copy the MACOS_LIBRARY_PATH to DYLD_LIBRARY_PATH.
			Map<String, String> environment = System.getenv();
			if (environment.containsKey("MACOS_LIBRARY_PATH") && !environment.containsKey("DYLD_LIBRARY_PATH")) {
				builder.environment().put("DYLD_LIBRARY_PATH", environment.get("MACOS_LIBRARY_PATH"));
			}
						
			// Start the process
			this.process = builder.start();
															
		} catch (IOException e) {
			LogInfo.getInstance().getLogger().severe("Process cannot be launched for application " + this.getNameId() + " : " + e.getMessage());
		}
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
	
	/**
	 * Launches the stop executable.
	 * It is not blocking.
	 */
	public void executeStop(String PID) {
		
		if (stopExecutable == null) {
			return;
		}
		
		LogInfo.getInstance().getLogger().fine("Launching stop executable for application " + this.getNameId());
		
		// Build the command arguments
		ArrayList<String> commandList = new ArrayList<String>();
		commandList.add(stopExecutable);
		
		if (stopArgs != null) {
			for (int i = 0; i < stopArgs.length; i++) {
				String arg = stopArgs[i].replace("$PID", PID);
				commandList.add(arg);
			}
		}
		
		// Prepare the command
		String command[] = new String[commandList.size()];
		commandList.toArray(command);
	
		String commandString = String.join(" ", command);
		LogInfo.getInstance().getLogger().fine("Start " + commandString);
			
		ProcessBuilder builder = new ProcessBuilder(command);
		
		// Set the working directory
		if (getDirectory() != null) {
			builder.directory(new File(getDirectory()));
		}	
		
		try {
			builder.start();
			
		} catch (IOException e) {
			LogInfo.getInstance().getLogger().severe("Stop executable is not launched for application " + this.getNameId() + " : " + e.getMessage());
			e.printStackTrace();
		}
		LogInfo.getInstance().getLogger().info("Launched stop executable for application " + this.getNameId());
	}
	
	public synchronized void kill() {
		if (process != null) {
			process.destroyForcibly();
		}	
	}
	
	public synchronized void reset() {
		process = null;
		hasToStop = false;
		hasToStopImmediately = false;
	}	
	
	/**
	 * @override
	 */
	public String toString() {
		return super.toString() + "\napplicationState=" + Converter.toString(this.applicationState) + "\nprocessState=" + this.processState + "\nhasToStop=" + this.hasToStop() + "\nshowStream=" + this.hasStream() + "\nstreamPort=" + this.getStreamPort() + "\nwriteStream=" + this.isWriteStream() + "\nid=" + this.getId();
	}

}