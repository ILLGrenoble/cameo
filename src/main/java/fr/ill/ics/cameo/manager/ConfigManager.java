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
import java.util.TreeSet;

public final class ConfigManager {

	private final static ConfigManager instance = new ConfigManager();

	private int maxNumberOfApplications;
	private boolean debugMode = false;
	private String host;
	private int port;
	private int streamPort;
	private TreeSet<Integer> openPorts = new TreeSet<Integer>();
	private int pollingTime;
	private String logPath;
	
	private ConfigManager() {
		super();
	}

	public final static ConfigManager getInstance() {
		return instance;
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

	public int getPollingTime() {
		return pollingTime;		
	}

	public void setPollingTime(int pollingTime) {
		this.pollingTime = pollingTime;
	}
	
	public int getPort() {
		return port;
	}

	public int getMaxNumberOfApplications() {
		return maxNumberOfApplications;
	}

	public void setMaxNumberOfApplications(String number) {
		try {
			if (number == null) {
				maxNumberOfApplications = 65536;
			} else {
				maxNumberOfApplications = Integer.parseInt(number);
			}
			
			if (maxNumberOfApplications <= 0) {
				throw new NumberFormatException("Error, the property 'max_applications' must be strictly positive");
			}

		} catch (java.lang.NumberFormatException e) {
			System.err.println("Error, the property 'max_applications' is required");
			System.exit(-1);
		}
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(String mode) {
		if (mode == null) {
			debugMode = false;
		} else if (mode.equalsIgnoreCase("ON")) {
			debugMode = true;
		} else if (mode.equalsIgnoreCase("OFF")) {
			debugMode = false;
		}
	}

	public int getNextPort() {

		int port = streamPort + 1;
		while (true) {
			
			if (openPorts.contains(port)) {
				port++;
			} else {
				// found a port
				openPorts.add(port);
				break;
			}
		}
				
		return port;
	}
	
	public void removePort(int port) {
		if (openPorts.contains(port)) {
			openPorts.remove(port);
		}
	}
	
	public int getStreamPort() {
		return streamPort;
	}
	
	public void setFirstPort(String firstPort) {
		try {
			port = Integer.parseInt(firstPort);
			streamPort = port + 1;
			if (streamPort <= 1024 || streamPort >= 40000) {
				System.err.println("Error, the property 'port' must be a value between 1025 and 39999");
				System.exit(-1);
			}

		} catch (java.lang.NumberFormatException e) {
			System.err.println("Error, the property 'port' is required");
			System.exit(-1);
		}
	}
	
	public void setHost(String host) {
		if (host == null) {
			try {
				this.host = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				this.host = "localhost";
			}
		} else {
			this.host = host;
		}
	}

	public String getEndpoint() {
		return "tcp://*:" + port;
	}
	
	public String getHostEndpoint() {
		return "tcp://" + host + ":" + port;
	}

}