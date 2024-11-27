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
 * Exception when writing the input stream.
 */
public class WriteException extends RemoteException {
	
	private static final long serialVersionUID = -9157515657163509208L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public WriteException(String message) {
		super(message);
	}
}