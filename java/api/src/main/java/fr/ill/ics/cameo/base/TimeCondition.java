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

package fr.ill.ics.cameo.base;

/**
 * Class defining a time condition.
 */
public class TimeCondition {

	private boolean notified = false;

	/**
	 * Waits for the time.
	 * @param timeMs Time in milliseconds.
	 * @return True if notification occurred during the wait.
	 */
	public synchronized boolean waitFor(int timeMs) {
		
		try {
			super.wait(timeMs);
		}
		catch (InterruptedException e) {
			return true;
		}
		
		return notified;
	}
	
	/**
	 * Notifies the condition. It unblocks the waitFor() call.
	 */
	public synchronized void notifyCondition() {
		notified = true;
		notify();
	}
}
