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
 * Exception for connection timeout.
 */
public class ConnectionTimeout extends RuntimeException {

	private static final long serialVersionUID = 2620802382339649353L;

	/**
	 * Constructor.
	 */
	public ConnectionTimeout(String endpoint) {
		super("Timeout while connecting " + endpoint);
	}
}