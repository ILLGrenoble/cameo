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
 * Interface defining an interface for objects that ping.
 */
public interface IPingable {

	/**
	 * Pings the object.
	 * @return True if there is no error.
	 */
	boolean ping();
}