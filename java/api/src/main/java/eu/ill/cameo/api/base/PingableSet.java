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
		
		System.out.println("Added pingable " + pingable);
	}
	
	/**
	 * Removes a IPingable object.
	 * @param pingable The IPingable object.
	 */
	public void remove(IPingable pingable) {
		synchronized (pingableSet) {
			pingableSet.remove(pingable);
		}	
		
		System.out.println("Removed pingable " + pingable);
	}
	
	/**
	 * Pings all the IPingable objects.
	 * @param timeout The timeout.
	 */
	public void pingAll(int timeout) {
		
		synchronized (pingableSet) {
			// The iteration must be in the synchronized block.
			Iterator<IPingable> i = pingableSet.iterator();
			while (i.hasNext()) {
				IPingable pingable = i.next();
				System.out.println("Pinging " + pingable);
				boolean pong = pingable.ping(timeout);
				
				if (pong) {
					System.out.println("Pong for " + pingable);
				}
				else {
					System.out.println("No pong for " + pingable);
				}
			}
		}
	}
	
}