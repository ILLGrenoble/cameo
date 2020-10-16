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

import java.util.HashMap;

public final class PortManager {

	private final static PortManager instance = new PortManager();
	private int basePort;
	
	/**
	 * Status of an reserved port.
	 */
	private enum Status {ASSIGNED, UNAVAILABLE};
	
	/**
	 * State of an reserved port.
	 */
	private static class State {
		Status status = Status.ASSIGNED;
	}
	
	/**
	 * Map of the reserved ports.
	 */
	private HashMap<Integer, State> reservedPorts = new HashMap<Integer, State>();
	
	/**
	 * Constructor.
	 */
	private PortManager() {
		super();
	}

	/**
	 * Get the instance.
	 * @return the instance
	 */
	public final static PortManager getInstance() {
		return instance;
	}
	
	/**
	 * Set the base port.
	 * @param port the base port
	 */
	public void setBasePort(int port) {
		basePort = port;
	}

	/**
	 * Request a non-reserved port. However the port can be unavailable because another application opened it.
	 * @return a port
	 */
	public int requestPort() {

		// Loop from the base port.
		int port = basePort;
		while (true) {
			
			if (reservedPorts.containsKey(port)) {
				port++;
			}
			else {
				// Found a port.
				reservedPorts.put(port, new State());
				break;
			}
		}
				
		return port;
	}
	
	/**
	 * Remove the port.
	 * @param port the port to remove
	 */
	public void removePort(int port) {
		reservedPorts.remove(port);
	}
	
	/**
	 * Set the port unavailable.
	 * @param port the port to set unavailable
	 */
	public void setPortUnavailable(int port) {
		if (reservedPorts.containsKey(port)) {
			reservedPorts.get(port).status = Status.UNAVAILABLE;
		}
	}

}