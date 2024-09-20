package eu.ill.cameo.server.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import eu.ill.cameo.common.strings.ApplicationIdentity;
import eu.ill.cameo.common.strings.ApplicationWithStarterIdentity;
import eu.ill.cameo.common.strings.Endpoint;
import eu.ill.cameo.processhandle.ProcessHandlerImpl;
import eu.ill.cameo.server.manager.OperatingSystem.OS;

public class RegisteredApplication extends Application {

	private java.lang.Process process;
	private String[] args;
	private ApplicationIdentity starter;
	private int starterProxyPort;
	private boolean starterLinked;
	
	public RegisteredApplication(Endpoint endpoint, int id, ApplicationConfig config, String[] args, ApplicationIdentity starter, int starterProxyPort, boolean starterLinked) {
		super(endpoint, id);
		
		// Set the config.
		this.setName(config.getName());
		this.setDescription(config.getDescription());
		this.setDirectory(config.getDirectory());
		this.setStartingTime(config.getStartingTime());
		this.setLogPath(config.getLogPath());
		this.setOutputStream(config.hasOutputStream());
		this.setOutputStreamPort(config.getOutputStreamPort());
		this.setStoppingTime(config.getStoppingTime());
		this.setRunMultiple(config.runSingle());
		this.setRunMaxApplications(config.runMaxApplications());
		this.setRestart(config.isRestart());
		this.setInfoArg(config.hasInfoArg());

		this.setStartExecutable(config.getStartExecutable());
		this.setStartArgs(config.getStartArgs());
		this.setStopExecutable(config.getStopExecutable());
		this.setStopArgs(config.getStopArgs());
		this.setErrorExecutable(config.getErrorExecutable());
		this.setErrorArgs(config.getErrorArgs());
		
		this.setEnvironmentVariables(config.getEnvironmentVariables());
		
		// A stop executable is a stop handler.
		this.hasStopHandler = (this.stopExecutable != null);
		
		this.args = args;
		this.starter = starter;
		this.starterProxyPort = starterProxyPort;
		this.starterLinked = starterLinked;
	}
	
	@Override
	public String[] getArgs() {
		return args;
	}
	
	@Override
	public boolean isRegistered() {
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
			if (hasInfoArg()) {
				// Pass the info in JSON format.
				ApplicationWithStarterIdentity identity = new ApplicationWithStarterIdentity(new ApplicationIdentity(name, id, endpoint), starter, starterProxyPort, starterLinked);

				// On Windows, " are removed, so we need to force their presence by escaping.
				String identityString = identity.toJSONString();
				if (OperatingSystem.get() == OS.WINDOWS) {
					identityString = identityString.replaceAll("[\"]", "\\\\\"");
				}
				
				commandList.add(identityString);
			}
			
			// Prepare the command
			String command[] = new String[commandList.size()];
			commandList.toArray(command);

			Log.logger().info("Application " + this.getNameId() + " executes " + commandListToString(command));
			
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
						
			Log.logger().info("Application " + this.getNameId() + " has pid " + this.processHandle.getPid());
		}
		catch (IOException e) {
			Log.logger().severe("Application " + this.getNameId() + " cannot be executed: " + e.getMessage());
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
		
		boolean argPID = false;
		
		// Use of $PID is deprecated.
		if (stopArgs != null) {
			for (int i = 0; i < stopArgs.length; i++) {
				
				if ("$PID".equals(stopArgs[i])) {
					argPID = true;
				}
				
				String arg = stopArgs[i].replace("$PID", pid);
				commandList.add(arg);
			}
		}
		
		// Add the PID if not present.
		if (!argPID) {
			commandList.add(pid);
		}
				
		// Prepare the command
		String command[] = new String[commandList.size()];
		commandList.toArray(command);
	
		Log.logger().info("Application " + this.getNameId() + " executes " + commandListToString(command));
			
		ProcessBuilder builder = new ProcessBuilder(command);
		
		// Set the working directory
		if (getDirectory() != null) {
			builder.directory(new File(getDirectory()));
		}
		
		// Add the environment variables from the application file.
		builder.environment().putAll(getEnvironmentVariables());
		
		try {
			builder.start();
		}
		catch (IOException e) {
			Log.logger().severe("Application " + this.getNameId() + " cannot execute stop process: " + e.getMessage());
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
