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