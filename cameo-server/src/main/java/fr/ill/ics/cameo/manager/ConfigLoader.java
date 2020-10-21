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

	protected List<ApplicationConfig> applicationList;

	public ConfigLoader(String path) {
		loadXml(buildXml(path));
	}

	public ConfigLoader(InputStream configStream) {
		loadXml(buildXml(configStream));
	}

	private String[] loadArgs(Element item) {
		
		String argsString = item.getAttributeValue("args");
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
		List<Element> args = item.getChildren("arg");
		String[] finalArgs = new String[argsLength + args.size()];
		
		int i = 0;
		while (i < argsLength) {
			finalArgs[i] = startArgs[i];
			++i;
		}
		
		for (Element arg : args) {
			finalArgs[i] = arg.getAttributeValue("value");
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
		ConfigManager.getInstance().setMaxNumberOfApplications(root.getAttributeValue("max_applications"));
		ConfigManager.getInstance().setHost(root.getAttributeValue("host"));
		ConfigManager.getInstance().setBasePort(root.getAttributeValue("port"));
		ConfigManager.getInstance().setLogPath(root.getAttributeValue("log_directory"));
		ConfigManager.getInstance().setLogLevel(root.getAttributeValue("log_level"));
		
		// Sleep time.
		int sleepTime = 5;
		String sleepTimeString = root.getAttributeValue("sleep_time");
		try {
			sleepTime = Integer.parseInt(sleepTimeString);
		} catch (NumberFormatException e) {
			// Set default value
		}
					
		ConfigManager.getInstance().setSleepTime(sleepTime);
		
		// Polling time.
		int pollingTime = 100;
		String pollingTimeString = root.getAttributeValue("polling_time");
		try {
			pollingTime = Integer.parseInt(pollingTimeString);
		} catch (NumberFormatException e) {
			// Set default value
			Log.logger().severe("Error while parsing polling time");
		}
		
		ConfigManager.getInstance().setPollingTime(pollingTime);
		
		// Get applications
		List<Element> listApplication = root.getChild("applications").getChildren("application");
		applicationList = new LinkedList<ApplicationConfig>();

		for (Element item : listApplication) {
			
			ApplicationConfig application = new ApplicationConfig();
			
			String applicationName = item.getAttributeValue("name");
			
			application.setName(applicationName);
			application.setDescription(item.getAttributeValue("description"));
			application.setDirectory(item.getAttributeValue("working_directory"));
			application.setErrorExecutable(item.getAttributeValue("error_command"));
			
			String logDirectory = item.getAttributeValue("log_directory");
			
			if ("default".equals(logDirectory)) {
				application.setLogPath(ConfigManager.getInstance().getLogPath());	
			} else {
				application.setLogPath(logDirectory);
			}
						
			application.setStartingTime(item.getAttributeValue("starting_time"));
			application.setStoppingTime(item.getAttributeValue("stopping_time"));
			application.setStopExecutable(item.getAttributeValue("stop_command"));
			application.setRunMultiple(item.getAttributeValue("multiple"));
			application.setStream(item.getAttributeValue("stream"));
			application.setPassInfo(item.getAttributeValue("pass_info"));
			application.setRestart(item.getAttributeValue("restart"));
			application.setEnvironmentFile(item.getAttributeValue("environment"));
			
			// Start command
			Element startItem = item.getChild("start");
			if (startItem == null) {
				Log.logger().severe("application node must contain a start node");
				continue;
			}
			application.setStartExecutable(startItem.getAttributeValue("executable"));
			String[] startArgs = loadArgs(startItem);
			application.setStartArgs(startArgs);
			
			// Stop command
			Element stopItem = item.getChild("stop");
			if (stopItem != null) {
				application.setStopExecutable(stopItem.getAttributeValue("executable"));
				String[] stopArgs = loadArgs(stopItem);
				application.setStopArgs(stopArgs);
			}

			// Error command
			Element errorItem = item.getChild("error");
			if (errorItem != null) {
				application.setErrorExecutable(errorItem.getAttributeValue("executable"));
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
	protected void showApplicationConfigs() {
		Log.logger().fine("List of applications");
		Iterator<ApplicationConfig> it = applicationList.iterator();
		while (it.hasNext()) {
			Log.logger().fine(((ApplicationConfig)it.next()).toString());
		}
	}
	
	public int getApplicationStreamPort(String name) {
		Iterator<ApplicationConfig> it = applicationList.iterator();
		while (it.hasNext()) {
			ApplicationConfig config = it.next();
			if (config.getName().equals(name)) {
				return config.getStreamPort();
			}
		}
		
		return -1;
	}

}