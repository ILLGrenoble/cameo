/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.common;

public class BadFormatException extends RuntimeException {

	private static final long serialVersionUID = 3943642846778611698L;

	public BadFormatException(String message) {
		super(message);
	}
}