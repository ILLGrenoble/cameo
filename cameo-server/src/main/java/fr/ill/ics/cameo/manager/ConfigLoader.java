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
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import fr.ill.ics.cameo.exception.UnknownApplicationException;

public abstract class ConfigLoader {

	private final String MAX_APPLICATIONS = "max_applications";
	private final String HOST = "host";
	private final String PORT = "port";
	private final String LOG_DIRECTORY = "log_directory";
	private final String LOG_LEVEL = "log_level";
	private final String SLEEP_TIME = "sleep_time";
	private final String POLLING_TIME = "polling_time";
	private final String APPLICATIONS = "applications";
	private final String APPLICATION = "application";
	private final String NAME = "name";
	private final String DESCRIPTION = "description";
	private final String WORKING_DIRECTORY = "working_directory";
	private final String DEFAULT = "default";
	private final String STARTING_TIME = "starting_time";
	private final String STOPPING_TIME = "stopping_time";
	private final String MULTIPLE = "multiple";
	private final String STREAM = "stream";
	private final String OUTPUT_STREAM = "output_stream";
	private final String PASS_INFO = "pass_info";
	private final String INFO_ARG = "info_arg";
	private final String RESTART = "restart";
	private final String ENVIRONMENT = "environment";
	private final String START = "start";
	private final String EXECUTABLE = "executable";
	private final String STOP = "stop";
	private final String ERROR = "error";
	private final String ARGS = "args";
	private final String ARG = "arg";
	private final String VALUE = "value";
	
	protected List<ApplicationConfig> applicationList;

	public ConfigLoader(String path) {
		loadXml(buildXml(path));
	}

	public ConfigLoader(InputStream configStream) {
		loadXml(buildXml(configStream));
	}

	private String[] loadArgs(Element item) {
		
		String argsString = item.getAttributeValue(ARGS);
		int argsLength = 0;
		String[] startArgs = null;
		
		// Process the args string and set it to null if it is empty.
		if (argsString != null) {
			argsString.trim();
			if (argsString.isEmpty()) {
				argsString = null;
			}
		}
		
		// Split the args string.
		if (argsString != null) {
			startArgs = argsString.split(" ");
			argsLength = startArgs.length;
		}
				
		// Process the arg tag and its value children.
		List<Element> args = item.getChildren(ARG);
		String[] finalArgs = new String[argsLength + args.size()];
		
		int i = 0;
		while (i < argsLength) {
			finalArgs[i] = startArgs[i];
			++i;
		}
		
		for (Element arg : args) {
			finalArgs[i] = arg.getAttributeValue(VALUE);
			++i;
		}
		
		return finalArgs;
	}
	
	private org.jdom2.Document buildXml(String path) {
		File configFile = new File(path);
		
		ConfigManager.getInstance().setConfigParent(configFile.getParent());
		
		// Load the configuration
		Log.logger().fine("Loading config");
		org.jdom2.Document configXML = null;
		SAXBuilder builder = new SAXBuilder();
		
		try {
			configXML = builder.build(configFile);
						
		} catch (JDOMException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		} catch (IOException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		}
		
		return configXML;
	}
	
	private org.jdom2.Document buildXml(InputStream stream) {
		
		// Load the configuration
		Log.logger().fine("Loading config");
		org.jdom2.Document configXML = null;
		SAXBuilder builder = new SAXBuilder();
		
		try {
			configXML = builder.build(stream);
						
		} catch (JDOMException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		} catch (IOException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		}
		
		return configXML;
	}
	
	/**
	 * Load config of applications from xml file
	 */
	private void loadXml(org.jdom2.Document configXML) {
		
		Element root = configXML.getRootElement();
		
		// Set the attributes
		ConfigManager.getInstance().setMaxNumberOfApplications(root.getAttributeValue(MAX_APPLICATIONS));
		ConfigManager.getInstance().setEndpoint(root.getAttributeValue(HOST), root.getAttributeValue(PORT)); 
		ConfigManager.getInstance().setLogPath(root.getAttributeValue(LOG_DIRECTORY));
		ConfigManager.getInstance().setLogLevel(root.getAttributeValue(LOG_LEVEL));
		
		// Sleep time.
		int sleepTime = 5;
		String sleepTimeString = root.getAttributeValue(SLEEP_TIME);
		try {
			sleepTime = Integer.parseInt(sleepTimeString);
		} catch (NumberFormatException e) {
			// Set default value
		}
					
		ConfigManager.getInstance().setSleepTime(sleepTime);
		
		// Polling time.
		int pollingTime = 100;
		String pollingTimeString = root.getAttributeValue(POLLING_TIME);
		try {
			pollingTime = Integer.parseInt(pollingTimeString);
		} catch (NumberFormatException e) {
			// Set default value
		}
		
		ConfigManager.getInstance().setPollingTime(pollingTime);
		
		// Get applications
		List<Element> listApplication = root.getChild(APPLICATIONS).getChildren(APPLICATION);
		applicationList = new LinkedList<ApplicationConfig>();

		for (Element item : listApplication) {
			
			ApplicationConfig application = new ApplicationConfig();
			
			String applicationName = item.getAttributeValue(NAME);
			
			application.setName(applicationName);
			application.setDescription(item.getAttributeValue(DESCRIPTION));
			application.setDirectory(item.getAttributeValue(WORKING_DIRECTORY));
			
			String logDirectory = item.getAttributeValue(LOG_DIRECTORY);
			
			if (DEFAULT.equals(logDirectory)) {
				application.setLogPath(ConfigManager.getInstance().getLogPath());	
			} else {
				application.setLogPath(logDirectory);
			}
						
			application.setStartingTime(item.getAttributeValue(STARTING_TIME));
			application.setStoppingTime(item.getAttributeValue(STOPPING_TIME));
			application.setRunMultiple(item.getAttributeValue(MULTIPLE));
			
			// Both attributes are accepted: output_stream or stream.
			String outputStreamValue = item.getAttributeValue(OUTPUT_STREAM);
			String streamValue = item.getAttributeValue(STREAM);
						
			if (streamValue != null) {
				application.setOutputStream(streamValue);
			}
			else {
				application.setOutputStream(outputStreamValue);
			}
			
			// Both attributes are accepted: info_arg or pass_info.
			String infoArg = item.getAttributeValue(INFO_ARG);
			String passInfo = item.getAttributeValue(PASS_INFO);
			
			if (passInfo != null) {
				application.setInfoArg(passInfo);
			}
			else {
				application.setInfoArg(infoArg);
			}
						
			application.setRestart(item.getAttributeValue(RESTART));
			application.setEnvironmentFile(item.getAttributeValue(ENVIRONMENT));
			
			// Start command
			Element startItem = item.getChild(START);
			if (startItem == null) {
				continue;
			}
			application.setStartExecutable(startItem.getAttributeValue(EXECUTABLE));
			String[] startArgs = loadArgs(startItem);
			application.setStartArgs(startArgs);
			
			// Stop command
			Element stopItem = item.getChild(STOP);
			if (stopItem != null) {
				application.setStopExecutable(stopItem.getAttributeValue(EXECUTABLE));
				String[] stopArgs = loadArgs(stopItem);
				application.setStopArgs(stopArgs);
			}

			// Error command
			Element errorItem = item.getChild(ERROR);
			if (errorItem != null) {
				application.setErrorExecutable(errorItem.getAttributeValue(EXECUTABLE));
				String[] errorArgs = loadArgs(errorItem);
				application.setErrorArgs(errorArgs);
			}
			
			applicationList.add(application);
		}
		
	}
	
	private void loadXml(InputStream configStream) {
		
		
	}

	/**
	 * Resturns the list of available applications.
	 * @return
	 */
	public List<ApplicationConfig> getAvailableApplications() {
		return applicationList;
	}
	
	/**
	 * verify command from user and return ApplicationConfig
	 * 
	 * @param commandArray
	 * @throws UnknownApplicationException
	 * @return ApplicationConfig
	 */
	protected ApplicationConfig verifyApplicationExistence(String name) throws UnknownApplicationException {

		ApplicationConfig ApplicationConfig = null;
		boolean isPresent = false;
		Iterator<ApplicationConfig> it = applicationList.iterator();

		while (it.hasNext()) {
			ApplicationConfig element = (ApplicationConfig) it.next();
			// if name is correct
			if (element.getName().equalsIgnoreCase(name)) {
				isPresent = true;
				ApplicationConfig = element;
				break;
			}
		}

		if (!isPresent) {
			throw new UnknownApplicationException();
		}
		return ApplicationConfig;
	}

	/**
	 * show config of applications, only used to debug
	 */
	protected void displayApplicationConfigs() {
		
		Log.logger().fine("List of applications");
		Iterator<ApplicationConfig> it = applicationList.iterator();
		while (it.hasNext()) {
			ApplicationConfig config = it.next();
			Log.logger().fine(config.getName() + " " + config.toJSONString());
		}
	}
	
	public int getApplicationStreamPort(String name) {
		Iterator<ApplicationConfig> it = applicationList.iterator();
		while (it.hasNext()) {
			ApplicationConfig config = it.next();
			if (config.getName().equals(name)) {
				return config.getOutputStreamPort();
			}
		}
		
		return -1;
	}

}