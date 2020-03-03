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



import java.util.concurrent.LinkedBlockingQueue;

/**
 * The EventListener abstract class receives status messages.
 * The application name is not required, in that case all status messages are received.
 * @author legoc
 *
 */
public class EventListener {

	private String name = null;
	private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
	
	/**
	 * Gets the name of the application.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void pushEvent(Event event) {
		try {
			eventQueue.put(event);
			
		} catch (InterruptedException e) {
			System.out.println("interrupted EventListener while putting");
		}
	}
	
	public Event popEvent(boolean blocking) {
		try {
			if (blocking) {
				return eventQueue.take();
			}
			return eventQueue.poll();
			
		} catch (InterruptedException e) {
			System.out.println("interrupted EventListener while popping");
			return null;
		}
	}
	
	public Event popEvent() {
		return popEvent(true);
	}
	
	public void cancel(int id) {
		try {
			eventQueue.put(new CancelEvent(id, name));
			
		} catch (InterruptedException e) {
			System.out.println("interrupted EventListener while putting");
		}
	}
	
	public void notifyTerminalState(int applicationId) {
		// do nothing here
	}
}