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

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Class defining an Event thread.
 * It forwards the events from the EventStreamSocket socket to the registered listeners.
 */
class EventThread extends Thread {

	private Server server;
	private EventStreamSocket socket;
		
	EventThread(Server server, EventStreamSocket socket) {
		this.server = server;
		this.socket = socket;
	}

	@Override	
	public void run() {
		
		try {
			while (true) {
				Event event = socket.receive();
				
				if (event == null) {
					// The stream is canceled.
					return;
				}
				
				// Forward the event to the listeners.
				ConcurrentLinkedDeque<FilteredEventListener> eventListeners = server.getEventListeners();
				for (FilteredEventListener listener : eventListeners) {
					
					// If the application name is null, all the status are pushed, otherwise, filter on the name.
					// We filter on the name because the id is not known at the registration.
					if (!listener.isFiltered()
						|| listener.getListener().getName().equals(event.getName())) {
						listener.getListener().pushEvent(event);
					}
				}
			}
		}
		finally {
			socket.terminate();
		}
	}
	
	/**
	 * Cancels the thread which unblocks the run() call.
	 */
	public void cancel() {
		socket.cancel();
	}
		
}