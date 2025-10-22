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

public class KeyAlreadyExistsException extends Exception {

	private static final long serialVersionUID = -1554083418570932263L;

	public String getMessage() {
		return "The key already exists";
	}
}