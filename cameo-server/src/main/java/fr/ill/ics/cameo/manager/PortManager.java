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

import java.util.TreeSet;

public final class PortManager {

	private final static PortManager instance = new PortManager();
	private int basePort;
	private TreeSet<Integer> assignedPorts = new TreeSet<Integer>();
	
	private PortManager() {
		super();
	}

	public final static PortManager getInstance() {
		return instance;
	}
	
	public void setBasePort(int port) {
		basePort = port;
	}

	public int getNextPort() {

		int port = basePort;
		while (true) {
			
			if (assignedPorts.contains(port)) {
				port++;
			} else {
				// found a port
				assignedPorts.add(port);
				break;
			}
		}
				
		return port;
	}
	
	public void removePort(int port) {
		if (assignedPorts.contains(port)) {
			assignedPorts.remove(port);
		}
	}

}