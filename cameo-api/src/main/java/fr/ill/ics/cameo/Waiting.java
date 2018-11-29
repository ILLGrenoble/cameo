package fr.ill.ics.cameo;
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



import fr.ill.ics.cameo.Application.This;

/**
 * The class implementation must be here to access the package protected member of This.
 * The class enables to cancel waitings and terminate the execution (used by This.terminate). 
 */
public abstract class Waiting {

	public void add() {
		// Add the waiting if This exists.
		if (This.impl != null) {
			This.impl.getWaitingSet().add(this);
		}	
	}
	
	public void remove() {
		// Remove the waiting if This exists.
		if (This.impl != null) {
			This.impl.getWaitingSet().remove(this);
		}
	}
	
	public abstract void cancel();
	public abstract void terminate();
}