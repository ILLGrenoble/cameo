package fr.ill.ics.cameo.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import fr.ill.ics.cameo.ProcessHandlerImpl;

public class ManagedApplication extends Application {

	private java.lang.Process process;
	protected String[] args;
	protected String starterReference;
	
	public ManagedApplication(String endpoint, int id, ApplicationConfig config, String[] args, String starterReference) {
		super(endpoint, id);
		
		// Set the config.
		this.setName(config.getName());
		this.setDescription(config.getDescription());
		this.setDirectory(config.getDirectory());
		this.setStartingTime(config.getStartingTime());
		this.setLogPath(config.getLogPath());
		this.setStream(config.hasStream());
		this.setStreamPort(config.getStreamPort());
		this.setStoppingTime(config.getStoppingTime());
		this.setRunMultiple(config.runsSingle());
		this.setRestart(config.isRestart());
		this.setPassInfo(config.isPassInfo());

		this.setStartExecutable(config.getStartExecutable());
		this.setStartArgs(config.getStartArgs());
		this.setStopExecutable(config.getStopExecutable());
		this.setStopArgs(config.getStopArgs());
		this.setErrorExecutable(config.getErrorExecutable());
		this.setErrorArgs(config.getErrorArgs());
		
		this.setEnvironmentVariables(config.getEnvironmentVariables());
		
		this.args = args;
		this.starterReference = starterReference;
	}
	
	@Override
	public String[] getArgs() {
		return args;
	}
	
	@Override
	public boolean isManaged() {
		return true;
	}
	
	@Override
	synchronized public Process getProcess() {
		return process;
	}
	
	@Override
	synchronized public boolean isAlive() {
		if (process != null) {
			return process.isAlive();
		}
		return false;
	}
	
	@Override
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
			Log.logger().info("Application " + this.getNameId() + " executes " + commandString);
			
			// Create the builder from the command
			ProcessBuilder builder = new ProcessBuilder(command);
			
			// Set the working directory
			if (getDirectory() != null) {
				builder.directory(new File(getDirectory()));
			}	
			
			// Standard output and error output are merged
			builder.redirectErrorStream(true); 

			// Add the environment variables from the application file.
			builder.environment().putAll(getEnvironmentVariables());
			
			// Start the process
			this.process = builder.start();
			
			// Get the process handle.
			this.processHandle = new ProcessHandlerImpl(process);
															
		} catch (IOException e) {
			Log.logger().severe("Application " + this.getNameId() + " has not executed : " + e.getMessage());
		}
	}
	
	@Override
	public void executeStop() {
		
		if (stopExecutable == null) {
			return;
		}
		
		// Get the pid.
		String pid = process.pid() + "";
		
		// Build the command arguments
		ArrayList<String> commandList = new ArrayList<String>();
		commandList.add(stopExecutable);
		
		if (stopArgs != null) {
			for (int i = 0; i < stopArgs.length; i++) {
				String arg = stopArgs[i].replace("$PID", pid);
				commandList.add(arg);
			}
		}
		
		// Prepare the command
		String command[] = new String[commandList.size()];
		commandList.toArray(command);
	
		String commandString = String.join(" ", command);
		Log.logger().info("Application " + this.getNameId() + " executes " + commandString);
			
		ProcessBuilder builder = new ProcessBuilder(command);
		
		// Set the working directory
		if (getDirectory() != null) {
			builder.directory(new File(getDirectory()));
		}	
		
		try {
			builder.start();
			
		} catch (IOException e) {
			Log.logger().severe("Application " + this.getNameId() + " has not executed stop process : " + e.getMessage());
			e.printStackTrace();
		}
		Log.logger().info("Application " + this.getNameId() + " has stop process finished");
	}
	
	@Override
	public synchronized void kill() {
		if (process != null) {
			process.destroyForcibly();
		}	
	}
	
	@Override
	public synchronized void reset() {
		process = null;
		hasToStop = false;
		hasToStopImmediately = false;
	}	
}
