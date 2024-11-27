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
public class SynchronizationTimeout extends RuntimeException {

	private static final long serialVersionUID = 1267072480020853574L;

	/**
	 * Constructor.
	 */
	public SynchronizationTimeout(String message) {
		super(message);
	}
}