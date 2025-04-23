/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.server.exception;

public class MaxGlobalNumberOfApplicationsReached extends Exception {

	private static final long serialVersionUID = -564543760445565040L;
	private String name;
	
	public MaxGlobalNumberOfApplicationsReached(String name) {
		this.name = name;
	}

	public String getMessage() {
		return "Cannot start '" + name + "' because the maximum global number of running applications is reached";
	}
}