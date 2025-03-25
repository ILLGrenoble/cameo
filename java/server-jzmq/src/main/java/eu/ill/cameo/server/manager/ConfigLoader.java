/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.server.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.ill.cameo.server.exception.UnknownApplicationException;

public abstract class ConfigLoader {

	public final static String MAX_APPLICATIONS = "max_applications";
	public final static String HOST = "host";
	public final static String PORT = "port";
	public final static String PROXY_PORTS = "proxy_ports";
	public final static String LOG_DIRECTORY = "log_directory";
	public final static String LOG_LEVEL = "log_level";
	public final static String SLEEP_TIME = "sleep_time";
	public final static String POLLING_TIME = "polling_time";
	public final static String APPLICATIONS = "applications";
	public final static String APPLICATION = "application";
	public final static String NAME = "name";
	public final static String DESCRIPTION = "description";
	public final static String WORKING_DIRECTORY = "working_directory";
	public final static String DEFAULT = "default";
	public final static String STARTING_TIME = "starting_time";
	public final static String STOPPING_TIME = "stopping_time";
	public final static String MULTIPLE = "multiple";
	public final static String STREAM = "stream";
	public final static String OUTPUT_STREAM = "output_stream";
	public final static String PASS_INFO = "pass_info";
	public final static String INFO_ARG = "info_arg";
	public final static String RESTART = "restart";
	public final static String ENVIRONMENT = "environment";
	public final static String START = "start";
	public final static String EXECUTABLE = "executable";
	public final static String STOP = "stop";
	public final static String ERROR = "error";
	public final static String ARGS = "args";
	public final static String ARG = "arg";
	public final static String VALUE = "value";
	
	protected List<ApplicationConfig> applicationList;

	public ConfigLoader(String path) {
		loadXml(buildXml(path));
	}

	public ConfigLoader(InputStream configStream) {
		loadXml(buildXml(configStream));
	}

	private static String getElementAttribute(Element element, String attributeName) {
		
		if (element.hasAttribute(attributeName)) {
			return element.getAttribute(attributeName);
		}
		
		return null;
	}
	
	private static List<Element> getElementChildren(Element element, String tagName) {
		
		List<Element> result = new ArrayList<Element>();
		
		NodeList nList = element.getElementsByTagName(tagName);
		
		// Iterate the child elements.
		for (int temp = 0; temp < nList.getLength(); temp++) {      
			Node nNode = nList.item(temp);
	             
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {     
				Element eElement = (Element) nNode;
				result.add(eElement);
			}
		}
		
		return result;
	}
	
	private static Element getElementChild(Element element, String tagName) {
		
		NodeList nList = element.getElementsByTagName(tagName);
		
		// Iterate the child elements.
		for (int temp = 0; temp < nList.getLength(); temp++) {      
			Node nNode = nList.item(temp);
	             
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {     
				return (Element) nNode;
			}
		}
		
		return null;
	}
	
	private String[] loadArgs(Element item) {
		
		String argsString = getElementAttribute(item, ARGS);
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
		List<Element> args = getElementChildren(item, ARG);
		String[] finalArgs = new String[argsLength + args.size()];
		
		int i = 0;
		while (i < argsLength) {
			finalArgs[i] = startArgs[i];
			++i;
		}
		
		for (Element arg : args) {
			finalArgs[i] = getElementAttribute(arg, VALUE);
			++i;
		}
		
		return finalArgs;
	}
	
	private org.w3c.dom.Document buildXml(String path) {
		File configFile = new File(path);
		
		ConfigManager.getInstance().setConfigParent(configFile.getParent());
		
		// Load the configuration.
		Log.logger().fine("Loading config");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			
			return docBuilder.parse(configFile);	
		}
		catch (ParserConfigurationException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		}	
		catch (SAXException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		}
		catch (IOException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		}
		
		return null;
	}
	
	private org.w3c.dom.Document buildXml(InputStream stream) {
		
		// Load the configuration.
		Log.logger().fine("Loading config");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			
			return docBuilder.parse(stream);	
		}
		catch (ParserConfigurationException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		}	
		catch (SAXException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		}
		catch (IOException e) {
			Log.logger().severe("Loading config failed: " + e.getMessage());
		}
		
		return null;
	}
	
	public static void setProxyPorts(String proxyPortsString) {
		
		if (proxyPortsString != null) {
			String[] proxyPorts = proxyPortsString.split(",");
			
			if (proxyPorts.length < 3) {
				Log.logger().severe("Error: " + PROXY_PORTS + " must contain three ports");
			}
			else {
				ConfigManager.getInstance().setResponderProxyPort(proxyPorts[0].strip());
				ConfigManager.getInstance().setPublisherProxyPort(proxyPorts[1].strip());
				ConfigManager.getInstance().setSubscriberProxyPort(proxyPorts[2].strip());
				
				ConfigManager.getInstance().setProxies(true);
			}
		}
	}
	
	private void loadXml(org.w3c.dom.Document configXML) {
		
		Element root = configXML.getDocumentElement();
		
		// Set the base parameters.
		ConfigManager.getInstance().setMaxNumberOfApplications(getElementAttribute(root, MAX_APPLICATIONS));
		ConfigManager.getInstance().setEndpoint(getElementAttribute(root, HOST), getElementAttribute(root, PORT));
		
		// Get the proxy ports that defined in command line to override those defined in the config file.
		String proxyPortsOverride = ConfigManager.getInstance().getProxyPorts();
		if (proxyPortsOverride != null) {
			setProxyPorts(proxyPortsOverride);	
		}
		else {
			setProxyPorts(getElementAttribute(root, PROXY_PORTS));
		}
		
		ConfigManager.getInstance().setLogPath(getElementAttribute(root, LOG_DIRECTORY));
		ConfigManager.getInstance().setLogLevel(getElementAttribute(root, LOG_LEVEL));
		
		// Sleep time.
		int sleepTime = 5;
		String sleepTimeString = getElementAttribute(root, SLEEP_TIME);
		try {
			sleepTime = Integer.parseInt(sleepTimeString);
		}
		catch (NumberFormatException e) {
			// Set default value.
		}
					
		ConfigManager.getInstance().setSleepTime(sleepTime);
		
		// Polling time.
		int pollingTime = 100;
		String pollingTimeString = getElementAttribute(root, POLLING_TIME);
		try {
			pollingTime = Integer.parseInt(pollingTimeString);
		}
		catch (NumberFormatException e) {
			// Set default value.
		}
		
		ConfigManager.getInstance().setPollingTime(pollingTime);
		
		// Get applications.
		Element apps = getElementChild(root, APPLICATIONS);
		List<Element> listApplication = getElementChildren(apps, APPLICATION);
		applicationList = new LinkedList<ApplicationConfig>();

		for (Element item : listApplication) {
			
			ApplicationConfig application = new ApplicationConfig();
			
			String applicationName = getElementAttribute(item, NAME);
			
			application.setName(applicationName);
			application.setDescription(getElementAttribute(item, DESCRIPTION));
			application.setDirectory(getElementAttribute(item, WORKING_DIRECTORY));
			
			String logDirectory = getElementAttribute(item, LOG_DIRECTORY);
			
			// If the attribute is absent then there is no log.
			if (DEFAULT.equals(logDirectory)) {
				application.setLogPath(ConfigManager.getInstance().getLogPath());	
			}
			else {
				application.setLogPath(logDirectory);
			}
						
			application.setStartingTime(getElementAttribute(item, STARTING_TIME));
			application.setStoppingTime(getElementAttribute(item, STOPPING_TIME));
			application.setRunMultiple(getElementAttribute(item, MULTIPLE));
			
			// Both attributes are accepted: output_stream or stream.
			String outputStreamValue = getElementAttribute(item, OUTPUT_STREAM);
			String streamValue = getElementAttribute(item, STREAM);
						
			if (streamValue != null) {
				application.setOutputStream(streamValue);
			}
			else {
				application.setOutputStream(outputStreamValue);
			}
			
			// Both attributes are accepted: info_arg or pass_info.
			String infoArg = getElementAttribute(item, INFO_ARG);
			String passInfo = getElementAttribute(item, PASS_INFO);
			
			if (passInfo != null) {
				application.setInfoArg(passInfo);
			}
			else {
				application.setInfoArg(infoArg);
			}
						
			application.setRestart(getElementAttribute(item, RESTART));
			application.setEnvironmentFile(getElementAttribute(item, ENVIRONMENT));
			
			// Start command.
			Element startItem = getElementChild(item, START);
			if (startItem == null) {
				continue;
			}
			application.setStartExecutable(getElementAttribute(startItem, EXECUTABLE));
			String[] startArgs = loadArgs(startItem);
			application.setStartArgs(startArgs);
			
			// Stop command.
			Element stopItem = getElementChild(item, STOP);
			if (stopItem != null) {
				application.setStopExecutable(getElementAttribute(stopItem, EXECUTABLE));
				String[] stopArgs = loadArgs(stopItem);
				application.setStopArgs(stopArgs);
				
				checkStopArgs(stopArgs);
			}

			// Error command.
			Element errorItem = getElementChild(item, ERROR);
			if (errorItem != null) {
				application.setErrorExecutable(getElementAttribute(errorItem, EXECUTABLE));
				String[] errorArgs = loadArgs(errorItem);
				application.setErrorArgs(errorArgs);
			}
			
			applicationList.add(application);
		}
		
	}
	
	private void checkStopArgs(String[] args) {
		
		for (String arg : args) {
			if ("$PID".equals(arg)) {
				Log.logger().warning("Using the $PID argument is deprecated");
			}
		}
	}

	private void loadXml(InputStream configStream) {
	}

	public List<ApplicationConfig> getAvailableApplications() {
		return applicationList;
	}
	
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
			throw new UnknownApplicationException(name);
		}
		return ApplicationConfig;
	}

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