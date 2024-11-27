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

import java.util.List;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.InitException;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.This;

/**
 * The test is not a real test because the subscribers are not synchronized.
 * The test does not finish and the main application must be stopped manually.
 * It can block if the subscribers had not the time to really subscribe (underlying PUB/SUB). 
 * @author legoc
 *
 */
public class TestSubscriber {

	public static void wait(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e) {
		}
	}
	
	public static void main(String[] args) {
		
		This.init(args);
		
		String subscriberApplicationName = null;
		int N = 1;
		if (args.length > 2) {
			subscriberApplicationName = args[0];
			N = Integer.parseInt(args[1]);
			System.out.println("Subscriber application is " + subscriberApplicationName);
		}
		else {
			System.err.println("Arguments: [subscriber application name] [number of loops]");
			System.exit(-1);
		}
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 3) {
			useProxy = Boolean.parseBoolean(args[2]);
		}
		if (useProxy) {
			endpoint = "tcp://localhost:12000";
		}
		
		Server server = Server.create(endpoint, (useProxy ? Option.USE_PROXY : 0));
		server.init();
		
		try {
			for (int i = 0; i < N; i++) {
				
				// Create 5 subscribers.
				for (int j = 0; j < 5; ++j) {
					// Pass the name of the application in argument.
					String[] applicationArgs = {This.getName()};
					
					// Start the subscriber applications that can subscribe whereas the publisher is not created.
					App subscriberApplication = server.start(subscriberApplicationName, applicationArgs);
					
					System.out.println("Started application " + subscriberApplication);
				}
				
				// Sleep for 1s to let the subscribers wait.
				wait(1000);
				
				// The publisher is created after the applications that will wait for it.
				eu.ill.cameo.api.coms.Publisher publisher = eu.ill.cameo.api.coms.Publisher.create("publisher");
				
				System.out.println("Publisher ready ? " + publisher.isReady());
				publisher.init();
				System.out.println("Publisher ready ? " + publisher.isReady());
				
				// Try a second init.
				publisher.init();
				
				for (int k = 0; k < 20; ++k) {
	
					String ks = k + "";
					String data = "{" + k + ", " + k * k + "}";
					publisher.sendTwoParts(ks.getBytes(), data.getBytes());
					
					System.out.println("sent " + data);
					
					wait(100);
				}		
				
				// The publisher sends end so that the subscriber applications receive end of stream.
				publisher.sendEnd();
				
				// So we must kill all the subscribers.
				List<App> subscriberApplications = server.connectAll(subscriberApplicationName);
				for (App subscriberApplication : subscriberApplications) {
					subscriberApplication.waitFor();
				}
				
				publisher.terminate();
			}
		}
		catch (InitException e) {
			System.out.println("Publisher error");
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}