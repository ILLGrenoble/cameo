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
 * Exception for an unregistered application.
 */
public class UnregisteredApplicationException extends RuntimeException {

	private static final long serialVersionUID = -4856229974519178147L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public UnregisteredApplicationException(String message) {
		super(message);
	}
}