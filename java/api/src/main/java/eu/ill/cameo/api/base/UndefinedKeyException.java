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
 * Exception for an undefined key.
 */
public class UndefinedKeyException extends RemoteException {

	private static final long serialVersionUID = 4525518598669069859L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public UndefinedKeyException(String message) {
		super(message);
	}
}