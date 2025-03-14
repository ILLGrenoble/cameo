/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.test;

import eu.ill.cameo.api.base.This;


public class PublisherError {

	public static void main(String[] args) {

		This.init(args);
		
		int numberOfSubscribers = 1;
		if (args.length > 1) {
			numberOfSubscribers = Integer.parseInt(args[0]);
		}
		
		System.out.println("Number of subscribers is " + numberOfSubscribers);
		
		try {
			System.out.println("Creating publisher and waiting for " + numberOfSubscribers + " subscriber(s)...");
			
			// Create the publisher.
			eu.ill.cameo.api.coms.Publisher publisher = eu.ill.cameo.api.coms.Publisher.create("publisher");
			publisher.setWaitForSubscribers(numberOfSubscribers);
			
			// Synchronize with subscribers.
			publisher.init();
			
			System.out.println("Synchronized with the subscriber(s)");
			
			This.setRunning();
			
			// Sending data.
			for (int i = 0; i < 10; ++i) {
				publisher.sendString("message " + i);
			}
			
			System.exit(123);
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}