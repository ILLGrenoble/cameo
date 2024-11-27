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
 * Interface defining an a cancelable object.
 */
public interface ICancelable {
	/**
	 * Cancels the object.
	 */
	void cancel();

	/**
	 * Returns true if is canceled.
	 * \return True if is canceled.
	 */
	boolean isCanceled();
}