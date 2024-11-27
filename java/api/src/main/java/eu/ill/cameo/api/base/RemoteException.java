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
 * Base class for remote exception.
 */
public class RemoteException extends Exception {

	private static final long serialVersionUID = -7302321626481573911L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public RemoteException(String message) {
		super(message);
	}
}