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

import org.json.simple.JSONObject;

public class ApplicationConfig {

	protected String name;
	protected String description = "";
	protected String directory;
	protected int startingTime = 0;
	protected String logPath;
	protected boolean outputStream = true;
	protected int outputStreamPort = -1;
	protected int stoppingTime = 10; // Default value is 10s
	protected boolean runSingle;
	protected boolean restart = false;
	protected boolean infoArg;
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
			System.err.println("Error with attribute 'name' in configuration file, 'name' is necessary");
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
			System.err.println("Error with attribute 'executable' in configuration file, 'executable' is necessary");
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
			System.err.println("Error with value of attribute 'starting_time' in configuration file");
			System.exit(-1);
		}
	}

	public String getLogPath() {
		return logPath;
	}

	public void setLogPath(String outputPath) {
		this.logPath = outputPath;
	}
	
	public boolean hasOutputStream() {
		return outputStream;
	}

	public void setOutputStream(boolean value) {
		this.outputStream = value;
	}
	
	public void setOutputStream(String value) {
		if (value == null) {
			this.outputStream = true;
		} else if (value.equalsIgnoreCase("yes")) {
			this.outputStream = true;
		} else if (value.equalsIgnoreCase("no")) {
			this.outputStream = false;
		} else {
			System.err.println("Error with attribute 'output_stream' in configuration file");
			System.exit(-1);
		}
	}
		
	public int getOutputStreamPort() {
		return this.outputStreamPort;
	}

	public void setOutputStreamPort(int port) {
		this.outputStreamPort = port;
	}
	
	public void setStartingTime(int startingTime) {
		this.startingTime = startingTime;
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
				this.stoppingTime = 10;
			} else if (stoppingTime.equals(INF)) {
				this.stoppingTime = -1;
			} else {
				this.stoppingTime = Integer.parseInt(stoppingTime);
			}
			
		} catch (java.lang.NumberFormatException e) {
			System.err.println("Error with attribute 'stopping_time' in configuration file");
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
			System.err.println("Error with attribute 'multiple' in configuration file");
			System.exit(-1);
		}
	}
	
	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public void setRestart(String value) {
		if (value == null) {
			this.restart = false; 
		} else if (value.equalsIgnoreCase("yes")) {
			this.restart = true;
		} else if (value.equalsIgnoreCase("no")) {
			this.restart = false;
		} else {
			System.err.println("Error with attribute 'restart' in configuration file");
			System.exit(-1);
		}
	}
	
	public boolean runsSingle() {
		return runSingle;
	}

	public boolean isRestart() {
		return restart;
	}

	public boolean hasInfoArg() {
		return infoArg;
	}

	public void setInfoArg(boolean value) {
		this.infoArg = value;
	}
	
	public void setInfoArg(String value) {
		if (value == null) {
			this.infoArg = true;
		} else if (value.equalsIgnoreCase("yes")) {
			this.infoArg = true;
		} else if (value.equalsIgnoreCase("no")) {
			this.infoArg = false;	
			
		} else {
			System.err.println("Error with attribute 'info_arg' in configuration file");
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
			
			// Load the variables with string replacement.
			environmentVariables = Environment.loadVariables(variables);
		}
		catch (IOException e) {
			// Do nothing.
		}
	}
	
	public void setEnvironmentVariables(HashMap<String, String> variables) {
		environmentVariables = variables;
	}
	
	public HashMap<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}
	
	public String toJSONString() {
		
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
		
		JSONObject object = new JSONObject();
		
		object.put("name", name);
		object.put("description", description);
		object.put("directory", directory);

		object.put("multiple", !runSingle);
		object.put("restart", restart);
		object.put("infoArg", infoArg);

		object.put("logDirectory", logPath);
		object.put("outputStream", outputStream);
		object.put("outputStreamPort", outputStreamPort);
		
		object.put("startExecutable", startExecutable);
		object.put("startArgs", startArgsString);
		object.put("startingTime", startingTime);

		object.put("stoppingTime", stoppingTime);
		object.put("stopExecutable", stopExecutable);
		object.put("stopArgs", stopArgsString);

		object.put("errorExecutable", errorExecutable);
		object.put("errorArgs", errorArgsString);
		
		return object.toJSONString();
	}
	
	@Override
	public String toString() {
		return "ApplicationConfig " + toJSONString();
	}
	
}