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

public class UnknownApplicationException extends Exception {

	private static final long serialVersionUID = -5719153178597114747L;
	private String name;

	public UnknownApplicationException(String name) {
		this.name = name;
	}

	public String getMessage() {
		return "Unknown application '" + name + "'";
	}
}