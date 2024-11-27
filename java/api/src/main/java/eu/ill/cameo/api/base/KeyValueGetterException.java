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
 * Class defining an exception when getting a key value fails.
 */
public class KeyValueGetterException extends RemoteException {

	private static final long serialVersionUID = 8288957337240690562L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public KeyValueGetterException(String message) {
		super(message);
	}
}