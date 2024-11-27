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


public class PublisherLoop {

	public static void main(String[] args) {

		This.init(args);
				
		try {
			System.out.println("Creating publisher and waiting for 1 subscriber...");
			
			// Create the publisher.
			eu.ill.cameo.api.coms.Publisher publisher = eu.ill.cameo.api.coms.Publisher.create("publisher");
			publisher.setWaitForSubscribers(1);
			
			// Synchronize with subscribers.
			publisher.init();
			
			System.out.println("Synchronized with the subscriber");
			
			This.setRunning();
			
			// Sending data.
			while (!publisher.hasEnded()) {
				
				publisher.sendString("hello");
				
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}
			}
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}