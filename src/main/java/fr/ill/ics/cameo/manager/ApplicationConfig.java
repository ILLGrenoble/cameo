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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

public class ApplicationConfig {

	protected String name;
	protected String description = "";
	protected String directory;
	protected int startingTime = 0;
	protected int retries = 0;
	protected String logPath;
	protected boolean stream = true;
	protected int streamPort = -1;
	protected int stoppingTime;
	protected boolean runSingle;
	protected boolean restart = false;
	protected boolean passInfo;
	protected HashMap<String, String> environmentVariables = new HashMap<String, String>(); 
	
	protected String startExecutable;
	protected String[] startArgs;
	protected String stopExecutable;
	protected String[] stopArgs;
	protected String errorExecutable;
	protected String[] errorArgs;
		
	private static final String INF = "inf";
	private static final String ENVIRONMENT_SUFFIX = "-environment.properties";
	
	public ApplicationConfig() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		try {
			if (name == null) {
				throw new NullPointerException();
			}
			this.name = name;
		} catch (NullPointerException e) {
			System.err.println("Error with property 'name' in configuration file, 'name' is necessary");
			System.exit(-1);
		}
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		if (description == null) {
			this.description = "";
		} else {
			this.description = description;
		}
	}
	
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}
		
	public String getStartExecutable() {
		return startExecutable;
	}

	public void setStartExecutable(String executable) {
		try {
			if (executable == null) {
				throw new NullPointerException();
			}
			this.startExecutable = executable;
		} catch (NullPointerException e) {
			System.err.println("Error with property 'start_command' in configuration file, 'start_command' is necessary");
			System.exit(-1);
		}
	}
	
	public String[] getStartArgs() {
		return startArgs;
	}

	public void setStartArgs(String[] args) {
		startArgs = args;
	}
	
	public String getStopExecutable() {
		return stopExecutable;
	}

	public void setStopExecutable(String command) {
		this.stopExecutable = command;
	}

	public String[] getStopArgs() {
		return stopArgs;
	}

	public void setStopArgs(String[] args) {
		stopArgs = args;
	}
	
	public String getErrorExecutable() {
		return errorExecutable;
	}

	public void setErrorExecutable(String executable) {
		this.errorExecutable = executable;
	}
	
	public String[] getErrorArgs() {
		return errorArgs;
	}

	public void setErrorArgs(String[] args) {
		errorArgs = args;
	}
	

	public int getStartingTime() {
		return startingTime;
	}

	public void setStartingTime(String startingTime) {
		try {
			if (startingTime == null) {
				// default value
				this.startingTime = 0;
			} else if (startingTime.equals(INF)) {
				this.startingTime = -1;
			} else {
				this.startingTime = Integer.parseInt(startingTime);
			}
		} catch (java.lang.NumberFormatException e) {
			System.err.println("Error with property 'starting_time' in configuration file");
			System.exit(-1);
		}
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(String retries) {
		try {
			if (retries == null) {
				// default value
				this.retries = 0;
			} else {
				this.retries = Integer.parseInt(retries);
				if (this.retries < 0) {
					this.retries = 0;
				}
			}
		} catch (java.lang.NumberFormatException e) {
			System.err.println("Error with property 'retries' in configuration file");
			System.exit(-1);
		} catch (NullPointerException e) {
			System.err.println("Error with property 'retries' in configuration file, 'retries' is necessary");
			System.exit(-1);
		}
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String outputPath) {
		this.logPath = outputPath;
	}
	
	public boolean hasStream() {
		return stream;
	}

	public void setStream(boolean value) {
		this.stream = value;
	}
	
	public void setStream(String value) {
		if (value == null) {
			this.stream = runSingle;
		} else if (value.equalsIgnoreCase("yes")) {
			this.stream = true;
			
			if (!runSingle) {
				stream = false;
				LogInfo.getInstance().getLogger().warning("The application " + name + " cannot have multiple instances and show stream");	
			}
			
		} else if (value.equalsIgnoreCase("no")) {
			this.stream = false;
		} else {
			System.err.println("Error with property 'output_stream' in configuration file");
			System.exit(-1);
		}
	}
		
	public int getStreamPort() {
		return this.streamPort;
	}

	public void setStreamPort(int port) {
		this.streamPort = port;
	}
	
	public void setStartingTime(int startingTime) {
		this.startingTime = startingTime;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public int getStoppingTime() {
		return stoppingTime;
	}

	public void setStoppingTime(int stoppingTime) {
		this.stoppingTime = stoppingTime;
	}

	public void setStoppingTime(String stoppingTime) {
		try {
			if (stoppingTime == null) {
				this.stoppingTime = 0;
			} else if (stoppingTime.equals(INF)) {
				this.stoppingTime = -1;
			} else {
				this.stoppingTime = Integer.parseInt(stoppingTime);
			}
			
		} catch (java.lang.NumberFormatException e) {
			System.err.println("Error with property 'stopping_time' in configuration file");
			System.exit(-1);
		}
	}

	public void setRunMultiple(boolean runSingle) {
		this.runSingle = runSingle;
	}

	public void setRunMultiple(String value) {
		if (value == null) {
			this.runSingle = false;
		}
		else if (value.equalsIgnoreCase("no")) {
			this.runSingle = true;
		} else if (value.equalsIgnoreCase("yes")) {
			this.runSingle = false;
		} else {
			System.err.println("Error with property 'multiple' in configuration file");
			System.exit(-1);

		}
	}
	
	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public void setRestart(String value) {
		if (value == null) {
			this.restart = true; 
		} else if (value.equalsIgnoreCase("yes")) {
			this.restart = true;
		} else if (value.equalsIgnoreCase("no")) {
			this.restart = false;
		} else {
			System.err.println("Error with property 'restart' in configuration file");
			System.exit(-1);
		}
	}
	
	public boolean runsSingle() {
		return runSingle;
	}

	public boolean isRestart() {
		return restart;
	}

	public boolean isPassInfo() {
		return passInfo;
	}

	public void setPassInfo(boolean passInfo) {
		this.passInfo = passInfo;
	}
	
	public void setPassInfo(String passInfo) {
		if (passInfo == null) {
			this.passInfo = true;
		} else if (passInfo.equalsIgnoreCase("yes")) {
			this.passInfo = true;
		} else if (passInfo.equalsIgnoreCase("no")) {
			this.passInfo = false;	
			
		} else {
			System.err.println("Error with property 'pass_info' in configuration file");
			System.exit(-1);
		}
	}

	public void setEnvironmentFile(String filename) {
		
		String environmentFilename;
		
		if (filename == null) {
			environmentFilename = name + ENVIRONMENT_SUFFIX;
		}
		else {
			environmentFilename = filename;
		}
		
		// Test if the environment file is absolute.
		File file = new File(environmentFilename);
		
		if (file.isAbsolute()) {
			if (!file.exists()) {
				return;
			}
			
			// We keep the file as is.
		}
		else {
			
			if (ConfigManager.getInstance().getConfigParent() != null) {
				
				// We try with the parent config file as parent directory.
				file = new File(ConfigManager.getInstance().getConfigParent(), environmentFilename);
				
				if (!file.exists()) {
					file = new File(environmentFilename);
					if (!file.exists()) {
						return;
					}
				}
			}
		}
			
		try {
			FileInputStream input = new FileInputStream(file);
			Properties variables = new Properties();
			variables.load(input);

			// Copying the content to the hash map.
			for (Entry<Object, Object> e : variables.entrySet()) {
				
				if (e.getKey() instanceof String && e.getValue() instanceof String) {
					environmentVariables.put((String) e.getKey(), (String) e.getValue());		
				}
			}
			
			System.out.println("Loaded environment file " + file);
			
		} catch (IOException e) {
			// Do nothing.
		}
	}
	
	public void setEnvironmentVariables(HashMap<String, String> variables) {
		environmentVariables = variables;
	}
	
	public HashMap<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}
	
	@Override
	public String toString() {
		String startArgsString = "";
		if (startArgs != null) {
			startArgsString = String.join(" ", startArgs);
		}
		
		String errorArgsString = "";
		if (errorArgs != null) {
			errorArgsString = String.join(" ", errorArgs);
		}
				
		String stopArgsString = "";
		if (stopArgs != null) {
			stopArgsString = String.join(" ", stopArgs);
		}	
		
		return "ApplicationConfig [name=" + name + ", description=" + description + ", directory=" + directory + ", startCommand=" + startExecutable + ", startArgs=" + startArgsString + ", errorComand=" + errorExecutable + ", errorArgs=" + errorArgsString + ", startingTime=" + startingTime + ", retries=" + retries + ", logDirectory=" + logPath + ", stream=" + stream + ", streamPort=" + streamPort + ", stopTimeout=" + stoppingTime + ", stopCommand=" + stopExecutable + ", stopArgs=" + stopArgsString + ", runSingle=" + runSingle + ", restart=" + restart + ", passInfo=" + passInfo + "]";
	}
	
}