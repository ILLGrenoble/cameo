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

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.This;


public class TestSubscriberTimeout {

	public static void main(String[] args) {

		This.init(args);
		
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			numberOfTimes = Integer.parseInt(args[0]);
		}
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 2) {
			useProxy = Boolean.parseBoolean(args[1]);
		}
		if (useProxy) {
			endpoint = "tcp://localhost:12000";
		}
		
		Server server = Server.create(endpoint, (useProxy ? Option.USE_PROXY : 0));
		
		try {
			server.init();
			
			Thread td = new Thread(() -> {
				
				// Get this app.
				final App thisApp = server.connect(This.getName());
				
				// Create a subscriber to the application.
				eu.ill.cameo.api.coms.Subscriber subscriber = eu.ill.cameo.api.coms.Subscriber.create(thisApp, "publisher");
				subscriber.init();
				
				System.out.println("Created subscriber " + subscriber);
				
				String data = subscriber.receiveString();
				if (data != null) {
					System.out.println("Received " + data);
				}
				
				subscriber.setTimeout(500);
				
				data = subscriber.receiveString();
				if (data != null) {
					System.out.println("Received " + data);
				}
				else {
					System.out.println("Has not received data, has timedout " + subscriber.hasTimedout());
				}
				
				data = subscriber.receiveString();
				if (data != null) {
					System.out.println("Received " + data);
				}
				else {
					System.out.println("Has not received data, has timedout " + subscriber.hasTimedout());
				}
				
			});
			td.start();
			
			eu.ill.cameo.api.coms.Publisher publisher = eu.ill.cameo.api.coms.Publisher.create("publisher");
			publisher.setSyncSubscribers(true);
			publisher.setWaitForSubscribers(1);
			publisher.init();
		
			publisher.sendString("first message");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
						
			publisher.sendString("message after timeout");
			
			System.out.println("Wait for subscriber termination");
			
			try {
				td.join();
			} catch (InterruptedException e) {
			}
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}