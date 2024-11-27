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


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Class defining a set of waitings.
 */
public class WaitingSet {

	private Set<Waiting> waitingSet = new HashSet<Waiting>();

	/**
	 * Adds a Waiting object.
	 * @param waiting The Waiting object.
	 */
	public void add(Waiting waiting) {
		synchronized (waitingSet) {
			waitingSet.add(waiting);
		}	
	}
	
	/**
	 * Removes a Waiting object.
	 * @param waiting The Waiting object.
	 */
	public void remove(Waiting waiting) {
		synchronized (waitingSet) {
			waitingSet.remove(waiting);
		}	
	}
	
	/**
	 * Cancels all the waitings.
	 */
	public void cancelAll() {

		synchronized (waitingSet) {
			// The iteration must be in the synchronized block.
			Iterator<Waiting> i = waitingSet.iterator();
			while (i.hasNext()) {
				Waiting waiting = i.next();
				waiting.cancel();
			}
		}
	}

	/**
	 * Terminates all the waitings.
	 */
	public void terminateAll() {
		
		// Copy the waitings because terminate removes the Waiting object from waitingSet.
		Set<Waiting> waitingSetCopy = new HashSet<Waiting>();
		
		synchronized (waitingSet) {
			waitingSetCopy.addAll(waitingSet);
		}
			
		// The iteration must be in the synchronized block.
		Iterator<Waiting> i = waitingSetCopy.iterator();
		while (i.hasNext()) {
			i.next().terminate();	
		}	
	}
	
}