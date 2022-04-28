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
import java.util.HashSet;

public final class PortManager {

	private final static PortManager instance = new PortManager();
	private int basePort;
	
	/**
	 * Status of an reserved port.
	 */
	public enum Status {ASSIGNED, UNAVAILABLE};
	
	/**
	 * State of an reserved port.
	 */
	public static class State {
		
		Status status = Status.ASSIGNED;
		String applicationName = null;
		Integer applicationId = null;
		
		public State(String applicationName, Integer applicationId) {
			this.applicationName = applicationName;
			this.applicationId = applicationId;
		}
	}
	
	/**
	 * Map of the reserved ports.
	 */
	private HashMap<Integer, State> reservedPorts = new HashMap<Integer, State>();
	
	/**
	 * Map of the application ports.
	 */
	private HashMap<Integer, HashSet<Integer>> applicationPorts = new HashMap<Integer, HashSet<Integer>>();
	
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
	 * Get the reserved ports.
	 * @return the reserved ports.
	 */
	public HashMap<Integer, State> getReservedPorts() {
		return reservedPorts;
	}

	/**
	 * Get the application ports. Create the entry if it does not exist.
	 * @param applicationId the application id
	 * @return the application ports
	 */
	private HashSet<Integer> getApplicationPorts(int applicationId) {
		
		if (!applicationPorts.containsKey(applicationId)) {
			applicationPorts.put(applicationId, new HashSet<Integer>());
		}
		return applicationPorts.get(applicationId);
	}
	
	/**
	 * Request a non-reserved port. However the port can be unavailable because another application opened it.
	 * @return a port
	 */
	public int requestPort(String applicationName, Integer applicationId) {

		// Loop from the base port.
		int port = basePort;
		while (true) {
			
			// Continue to iterate if the port is already reserved.
			if (reservedPorts.containsKey(port)) {
				port++;
			}
			else {
				// Found a port. Add it in the reserved ports and application ports.
				reservedPorts.put(port, new State(applicationName, applicationId));
				
				// Add the port in the application map if the id exists.
				if (applicationId != null) {
					getApplicationPorts(applicationId).add(port);
				}
				
				break;
			}
		}
				
		return port;
	}
			
	/**
	 * Remove the port.
	 * @param port the port to remove
	 * @return true if removed
	 */
	public boolean removePort(int port) {
		
		// Remove the port from the reserved list.
		State state = reservedPorts.remove(port);
		
		// Remove the state from application.
		if (state != null 
				&& state.status == Status.ASSIGNED
				&& state.applicationId != null) {
			getApplicationPorts(state.applicationId).remove(port);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Remove the ports of the application. 
	 * @param applicationId the application id
	 */
	public HashSet<Integer> removeApplication(int applicationId) {
		
		// Remove the ports of the application.
		HashSet<Integer> ports = getApplicationPorts(applicationId);
		
		for (Integer port : ports) {
			reservedPorts.remove(port);
		}
		
		// Remove the application.
		applicationPorts.remove(applicationId);
		
		return ports;
	}
	
	/**
	 * Set the port unavailable.
	 * @param port the port to set unavailable
	 */
	public void setPortUnavailable(int port) {
		
		if (reservedPorts.containsKey(port)) {
			State state = reservedPorts.get(port);
			
			// Set it unavailable.
			state.status = Status.UNAVAILABLE;
						
			// Remove from the application.
			if (state.applicationId != null) {
				getApplicationPorts(state.applicationId).remove(port);
			}
			
			// Do not set the application id and name to null, so that we can memorize the application which set the port unavailable;
		}
	}
	
}