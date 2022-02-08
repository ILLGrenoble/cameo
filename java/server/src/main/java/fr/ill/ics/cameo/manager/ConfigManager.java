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

import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.ill.ics.cameo.strings.Endpoint;

public final class ConfigManager {

	private final static ConfigManager instance = new ConfigManager();

	private int maxNumberOfApplications;
	private String logLevel = "FINE";
	private String configParent;
	private Endpoint endpoint;
	private int streamPort;
	private int sleepTime;
	private int pollingTime;
	private String logPath;
	private Endpoint proxyLocalEndpoint;
	private Endpoint proxyHostEndpoint;
	
	private ConfigManager() {
		super();
	}

	public final static ConfigManager getInstance() {
		return instance;
	}

	public void setConfigParent(String parent) {
		this.configParent = parent;
	}
	
	public String getConfigParent() {
		return configParent;
	}
	
	public String getLogPath() {
		return logPath;
	}
	
	public void setLogPath(String path) {
		if (path == null) {
			logPath = ".";
		} else {
			logPath = path;
		}	
	}

	public int getSleepTime() {
		return sleepTime;		
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	
	public int getPollingTime() {
		return pollingTime;		
	}

	public void setPollingTime(int pollingTime) {
		this.pollingTime = pollingTime;
	}
	
	public int getPort() {
		return endpoint.getPort();
	}
	
	public int getProxyPort() {
		return proxyLocalEndpoint.getPort();
	}
		
	public int getMaxNumberOfApplications() {
		return maxNumberOfApplications;
	}

	public void setMaxNumberOfApplications(String number) {
		try {
			if (number == null) {
				maxNumberOfApplications = 65536;
			}
			else {
				maxNumberOfApplications = Integer.parseInt(number);
			}
			
			if (maxNumberOfApplications <= 0) {
				throw new NumberFormatException("Error, the property 'max_applications' must be strictly positive");
			}
		}
		catch (java.lang.NumberFormatException e) {
			System.err.println("Error, the property 'max_applications' is required");
			System.exit(-1);
		}
	}
	
	public String getLogLevel() {
		return logLevel;
	}
	
	public void setLogLevel(String level) {
		if (level == null) {
			// Default value.
			logLevel = "INFO";
		}
		else if (level.equalsIgnoreCase("OFF")) {
			logLevel = "OFF";
		}
		else if (level.equalsIgnoreCase("INFO")) {
			logLevel = "INFO";
		}
		else if (level.equalsIgnoreCase("FINE")) {
			logLevel = "FINE";
		}
		else if (level.equalsIgnoreCase("FINER")) {
			logLevel = "FINER";
		}
		else if (level.equalsIgnoreCase("FINEST")) {
			logLevel = "FINEST";
		}
	}
	
	public void setStreamPort(int port) {
		streamPort = port;
	}
	
	public int getStreamPort() {
		return streamPort;
	}
	
	private int defineBasePort(String portString) {
		
		int port = 0;
		
		try {
			port = Integer.parseInt(portString);

			// Set the base port of the port manager.
			PortManager.getInstance().setBasePort(port + 1);
		}
		catch (java.lang.NumberFormatException e) {
			System.err.println("Error, the property 'port' is required");
			System.exit(-1);
		}
		
		return port;
	}
	
	private String defineHost(String hostString) {
		
		String host;
		
		// If host is provided, set it.
		if (hostString == null) {
			try {
				// Otherwise try to get the hostname.
				host = InetAddress.getLocalHost().getHostName();
			}
			catch (UnknownHostException e) {
				try {
					// Otherwise try to get the IP address.
					host = InetAddress.getLocalHost().getHostAddress();
				}
				catch (UnknownHostException e2) {
					// Otherwise set localhost.
					host = "localhost";
				}
			}
		// If host is IP.
		}
		else if (hostString.equals("IP")) {
			try {
				// Try to get the IP address.
				host = InetAddress.getLocalHost().getHostAddress();
			}
			catch (UnknownHostException e) {
				// Otherwise set localhost.
				host = "localhost";
			}
		}
		else {
			host = hostString;
		}
		
		return host;
	}

	public String getEndpoint() {
		return "tcp://*:" + endpoint.getPort();
	}
	
	public Endpoint getHostEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String hostString, String portString) {
		String host = defineHost(hostString);
		int port = defineBasePort(portString);

		endpoint = new Endpoint(host, port);
	}

	public void setProxyPort(String portString) {
		
		int port = 0;
		
		try {
			port = Integer.parseInt(portString);
		}
		catch (java.lang.NumberFormatException e) {
			System.err.println("Error, 'proxy_port' is not an integer");
		}
		
		String localhost;
		
		try {
			localhost = InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException e) {
			localhost = "127.0.0.1"; 
		}
		
		proxyLocalEndpoint = new Endpoint(localhost, port);
		proxyHostEndpoint = endpoint.withPort(port);
	}
	
	public Endpoint getProxyLocalEndpoint() {
		return proxyLocalEndpoint;		
	}
	
	public Endpoint getProxyHostEndpoint() {
		return proxyHostEndpoint;		
	}


}