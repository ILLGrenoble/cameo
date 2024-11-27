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
 * Base class for the waiting classes.
 * The class enables to cancel waitings and terminate the execution (used by This.terminate). 
 */
public abstract class Waiting {

	/**
	 * Adds this object to the set of waitings.
	 */
	public void add() {
		// Add the waiting if This exists.
		if (This.instance != null) {
			This.instance.getWaitingSet().add(this);
		}	
	}
	
	/**
	 * Removes this object from the set of waitings.
	 */
	public void remove() {
		// Remove the waiting if This exists.
		if (This.instance != null) {
			This.instance.getWaitingSet().remove(this);
		}
	}
	
	/**
	 * Cancels the waiting.
	 */
	public abstract void cancel();
	
	/**
	 * Terminates the waiting object.
	 */
	public abstract void terminate();
}