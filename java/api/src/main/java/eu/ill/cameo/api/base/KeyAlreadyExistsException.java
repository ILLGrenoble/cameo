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
 * Exception for a key that already exists.
 */
public class KeyAlreadyExistsException extends RemoteException {

	private static final long serialVersionUID = -3629600566663993697L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public KeyAlreadyExistsException(String message) {
		super(message);
	}
}