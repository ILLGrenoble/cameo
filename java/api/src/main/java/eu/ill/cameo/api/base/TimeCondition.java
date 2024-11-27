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