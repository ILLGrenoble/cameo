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

public class MaxNumberOfApplicationsReached extends Exception {

	private static final long serialVersionUID = -679186322182419615L;
	private String name;
	
	public MaxNumberOfApplicationsReached(String name) {
		this.name = name;
	}

	public String getMessage() {
		return "Cannot start '" + name + "' because its maximum number of running applications is reached";
	}
}