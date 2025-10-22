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

public class PortInfo {

	private int port;
	private String status;
	private String applicationNameId;
	
	public PortInfo(int port, String status, String applicationNameId) {
		super();
		this.port = port;
		this.status = status;
		this.applicationNameId = applicationNameId;
	}

	public int getPort() {
		return port;
	}

	public String getStatus() {
		return status;
	}

	public String getApplicationNameId() {
		return applicationNameId;
	}
	
	
}