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


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WaitingSet {

	private Set<Waiting> waitingSet = new HashSet<Waiting>();

	public void add(Waiting waiting) {
		synchronized (waitingSet) {
			waitingSet.add(waiting);
		}	
	}
	
	public void remove(Waiting waiting) {
		synchronized (waitingSet) {
			waitingSet.remove(waiting);
		}	
	}
	
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