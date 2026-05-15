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
 * Class defining a set of IPingables.
 */
public class PingableSet {

	private Set<IPingable> pingableSet = new HashSet<IPingable>();

	/**
	 * Adds a IPingable object.
	 * @param pingable The IPingable object.
	 */
	public void add(IPingable pingable) {
		synchronized (pingableSet) {
			pingableSet.add(pingable);
		}	
	}
	
	/**
	 * Removes a IPingable object.
	 * @param pingable The IPingable object.
	 */
	public void remove(IPingable pingable) {
		synchronized (pingableSet) {
			pingableSet.remove(pingable);
		}	
	}
	
	/**
	 * Pings all the IPingable objects.
	 */
	public void pingAll() {

		synchronized (pingableSet) {
			// The iteration must be in the synchronized block.
			Iterator<IPingable> i = pingableSet.iterator();
			while (i.hasNext()) {
				IPingable IPingable = i.next();
				IPingable.ping();
			}
		}
	}
	
}