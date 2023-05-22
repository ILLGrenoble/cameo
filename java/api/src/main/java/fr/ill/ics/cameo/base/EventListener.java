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


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The EventListener class receives event messages.
 * The application name is not required, in that case all messages are received.
 */
public class EventListener {

	private String name = null;
	private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
	
	/**
	 * Gets the application name.
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the listener i.e. the application name.
	 * It is used to filter the messages in the event thread because when the listener is registered, the application id is not known.
	 * @param name The application name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Pushes the event on the queue.
	 * @param event The event.
	 */
	public void pushEvent(Event event) {
		try {
			eventQueue.put(event);
		}
		catch (InterruptedException e) {
			System.out.println("interrupted EventListener while putting");
		}
	}
	
	/**
	 * Pops the event from the queue.
	 * @param blocking True if the call is blocking.
	 * @throws Timeout in case of timeout.
	 */
	public Event popEvent(boolean blocking, int timeout) {
		try {
			if (blocking) {
				if (timeout == -1) {
					return eventQueue.take();
				}
				else {
					Event event = eventQueue.poll(timeout, TimeUnit.MILLISECONDS);
					
					if (event != null) {
						return event;
					}
					
					throw new Timeout();
				}
			}
			return eventQueue.poll();
		}
		catch (InterruptedException e) {
			System.out.println("interrupted EventListener while popping");
			return null;
		}
	}
	
	/**
	 * Pops the event from the queue.
	 * @param blocking True if the call is blocking.
	 */
	public Event popEvent(boolean blocking) {
		return popEvent(blocking, -1);
	}
	
	/**
	 * Pops the event from the queue.
	 * @param timeout Timeout.
	 * @throws Timeout in case of timeout.
	 */
	public Event popEvent(int timeout) {
		return popEvent(true, timeout);
	}
	
	/**
	 * Pops the event from the queue.
	 */
	public Event popEvent() {
		return popEvent(true, -1);
	}

	/**
	 * Pushes a CancelEvent with application id.
	 * @param id The application id.
	 */
	public void cancel(int id) {
		try {
			eventQueue.put(new CancelEvent(id, name));
		}
		catch (InterruptedException e) {
			System.out.println("interrupted EventListener while putting");
		}
	}
}