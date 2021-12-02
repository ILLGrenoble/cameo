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

package fr.ill.ics.cameo.base.impl;

import java.util.concurrent.ConcurrentLinkedDeque;

import fr.ill.ics.cameo.base.Event;
import fr.ill.ics.cameo.base.EventListener;
import fr.ill.ics.cameo.base.EventStreamSocket;

/**
 * The EventThread class forwards the events from the EventStreamSocket socket to the registered listeners.
 */
class EventThread extends Thread {

	private ServerImpl server;
	private EventStreamSocket socket;
	
	EventThread(ServerImpl server, EventStreamSocket socket) {
		this.server = server;
		this.socket = socket;
	}
	
	public void run() {
		
		try {
			while (true) {
				Event event = socket.receive();
				
				if (event == null) {
					// The stream is canceled.
					return;
				}
				
				// Forward the event to the listeners.
				ConcurrentLinkedDeque<EventListener> eventListeners = server.getEventListeners();
				for (EventListener listener : eventListeners) {
					
					// If the application name is null, all the status are pushed, otherwise, filter on the name.
					// We filter on the name because the id is not known at the registration.
					if (listener.getName() == null
						|| listener.getName().equals(event.getName())) {
						listener.pushEvent(event);
					}
				}
			}
			
		} finally {
			socket.destroy();	
		}
	}
	
	public void cancel() {
		socket.cancel();
	}
		
}