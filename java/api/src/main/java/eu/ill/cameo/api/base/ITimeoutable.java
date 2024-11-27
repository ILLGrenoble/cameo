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
 * Interface defining an interface for objects that have timeout.
 */
public interface ITimeoutable {

	/**
	 * Sets the timeout.
	 * @param value The timeout.
	 */
	void setTimeout(int value);

	/**
	 * Gets the timeout.
	 * @return The timeout.
	 */
	int getTimeout();
}