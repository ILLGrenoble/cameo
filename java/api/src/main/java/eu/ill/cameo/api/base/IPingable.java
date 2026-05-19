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
	 * Enables the pingable object.
	 * @param value True if pinged.
	 */
	void setPinged(boolean value);
	
	/**
	 * Returns true if is enabled.
	 * @return true if is enabled.
	 */
	boolean isPinged();
	
	/**
	 * Pings the object.
	 * @param timeout The timeout.
	 * @return True if there is no error.
	 */
	boolean ping(int timeout);
}