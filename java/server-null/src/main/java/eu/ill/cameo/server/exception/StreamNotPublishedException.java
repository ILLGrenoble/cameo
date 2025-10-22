/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.server.exception;

public class StreamNotPublishedException extends Exception {

	private static final long serialVersionUID = -153342115687589400L;

	public String getMessage() {
		return "Stream is not published";
	}
}