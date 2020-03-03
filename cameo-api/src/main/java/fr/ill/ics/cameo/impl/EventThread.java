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

package fr.ill.ics.cameo.impl;

import java.util.concurrent.ConcurrentLinkedDeque;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.Event;
import fr.ill.ics.cameo.EventListener;
import fr.ill.ics.cameo.EventStreamSocket;
import fr.ill.ics.cameo.PortEvent;
import fr.ill.ics.cameo.PublisherEvent;
import fr.ill.ics.cameo.ResultEvent;
import fr.ill.ics.cameo.StatusEvent;

class EventThread extends Thread {

	private ServerImpl server;
	private EventStreamSocket socket;
	
	EventThread(ServerImpl server, EventStreamSocket socket) {
		this.server = server;
		this.socket = socket;
	}
	
	private void processStatusEvent(StatusEvent status) {
		// Test the terminal state
		int state = status.getState();
		boolean terminal = false;
		
		if (state == Application.State.SUCCESS 
				|| state == Application.State.STOPPED
				|| state == Application.State.KILLED
				|| state == Application.State.ERROR) {
			terminal = true;
		}
						
		// Send event to listeners.
		// The EventListener contains the name attribute but not the application id because 
		// the id is not available at the registration.
		ConcurrentLinkedDeque<EventListener> eventListeners = server.getEventListeners();
		for (EventListener listener : eventListeners) {
			
			// If the application name is null, all the status are pushed
			// otherwise, filter on the name
			if (listener.getName() == null 
				|| listener.getName().equals(status.getName())) {
				listener.pushEvent(status);
				
				// In case of terminal state, unregister the listener
				// otherwise a memory leak occurs with Instance classes
				if (terminal) {
					listener.notifyTerminalState(status.getId());
				}
			}
		}
	}
	

	private void processResultEvent(ResultEvent result) {
		
		// Send event to listeners.
		// The EventListener contains the name attribute but not the application id because 
		// the id is not available at the registration.
		ConcurrentLinkedDeque<EventListener> eventListeners = server.getEventListeners();
		for (EventListener listener : eventListeners) {
			
			// Filter on the name
			if (listener.getName() != null && listener.getName().equals(result.getName())) {
				listener.pushEvent(result);
			}
		}
	}
	
	private void processPublisherEvent(PublisherEvent publisher) {
						
		// Send event to listeners.
		// The EventListener contains the name attribute but not the application id because 
		// the id is not available at the registration.
		ConcurrentLinkedDeque<EventListener> eventListeners = server.getEventListeners();
		for (EventListener listener : eventListeners) {
			
			// If the application name is null, all the status are pushed
			// otherwise, filter on the name
			if (listener.getName() == null 
				|| listener.getName().equals(publisher.getName())) {
				listener.pushEvent(publisher);
			}
		}
	}
	
	private void processPortEvent(PortEvent port) {
		
		// Send event to listeners.
		// The EventListener contains the name attribute but not the application id because 
		// the id is not available at the registration.
		ConcurrentLinkedDeque<EventListener> eventListeners = server.getEventListeners();
		for (EventListener listener : eventListeners) {
			
			// If the application name is null, all the status are pushed
			// otherwise, filter on the name
			if (listener.getName() == null 
				|| listener.getName().equals(port.getName())) {
				listener.pushEvent(port);
			}
		}
	}
		
	public void run() {
		
		try {
			while (true) {
				Event event = socket.receive();
				
				if (event == null) {
					// The stream is canceled.
					return;
				}
				
				if (event instanceof StatusEvent) {
					processStatusEvent((StatusEvent)event);
					
				} else if (event instanceof ResultEvent) {
					processResultEvent((ResultEvent)event);
					
				} else if (event instanceof PublisherEvent) {
					processPublisherEvent((PublisherEvent)event);
					
				} else if (event instanceof PortEvent) {
					processPortEvent((PortEvent)event);
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