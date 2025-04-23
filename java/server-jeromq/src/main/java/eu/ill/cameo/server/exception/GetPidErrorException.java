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

public class GetPidErrorException extends Exception {
	
	private static final long serialVersionUID = -5139958494856857849L;
	private Exception exception;
	
	public GetPidErrorException(Exception exception) {
		this.exception = exception;
	}
	
	public Exception getException() {
		return exception;
	}
	
	public GetPidErrorException() {
		super();
	}
}