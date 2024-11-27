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
public class Timeout extends RuntimeException {

	private static final long serialVersionUID = -676054674793041235L;

	/**
	 * Constructor.
	 */
	public Timeout() {
		super("Timeout");
	}
}