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
 * Exception when the unexpected happened.
 */
public class UnexpectedException extends RuntimeException {

	private static final long serialVersionUID = 8526544898560843343L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public UnexpectedException(String message) {
		super(message);
	}
}