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

package fr.ill.ics.cameo.examples;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.coms.Publisher;

public class PublisherApp {
	
	public static String serializeToJSON(String message, int i) {
		
		JSONObject object = new JSONObject();
		
		object.put("message", message);
		object.put("value", i);
		
		return object.toJSONString();
	}
		
	public static void main(String[] args) {

		// Initialize cameo.
		This.init(args);
	
		// Define the stop handler to properly stop.
		This.handleStop(() -> {});
		
		int numberOfSubscribers = 1;
		
		Publisher publisher = null;
		
		try {
			// Create the publisher.
			publisher = Publisher.create("the-publisher");
			publisher.setSyncSubscribers(true);
			publisher.setWaitForSubscribers(numberOfSubscribers);

			System.out.println("Created publisher " + publisher);
			
			publisher.init();

			System.out.println("Synchronized with " + numberOfSubscribers + " subscriber(s)");
			
			// Set the state.
			This.setRunning();

			int i = 0;
			while (!This.isStopping()) {
			
				// Send a message.
				publisher.sendString(serializeToJSON("a message", i));
				i++;
				
				// Sleep for 1s.
				Thread.sleep(1000);
			}
		}
		catch (InterruptedException e) {
		}
		finally {
			// Terminate the publisher and This.
			publisher.terminate();
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}