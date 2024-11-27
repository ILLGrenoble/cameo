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

import java.time.Duration;
import java.time.Instant;

/**
 * Class defining a timeout helper to define timeouts for operations that have different steps.
 */
public class TimeoutCounter {

	private int value = -1;
	private Instant start;
	
	/**
	 * Constructor.
	 * @param value The time in milliseconds.
	 */
	public TimeoutCounter(int value) {
		this.value = value;
		start = Instant.now();
	}

	/**
	 * Returns the remaining time at this call.
	 * @return The remaining time in milliseconds.
	 */
	public int remains() {
		
		// Return no timeout if it is the case.
		if (value == -1) {
			return -1;
		}

		// Get the time elapsed since the creation of the object.
		int diffMs = (int)Duration.between(start, Instant.now()).toMillis();

		if (diffMs > value) {
			return 0;
		}

		return value - diffMs;
	}

}