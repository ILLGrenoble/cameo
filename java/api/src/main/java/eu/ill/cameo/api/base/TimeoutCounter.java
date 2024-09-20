/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.api.base;

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
