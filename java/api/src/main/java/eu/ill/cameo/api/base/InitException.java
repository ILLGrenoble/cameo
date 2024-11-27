/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.base;

/**
 * Exception for a responder creation.
 */
public class InitException extends RuntimeException {
	
	private static final long serialVersionUID = 770038622378735273L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public InitException(String message) {
		super(message);
	}
}