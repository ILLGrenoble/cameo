/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.examples;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.Publisher;

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