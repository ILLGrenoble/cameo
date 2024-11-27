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
import eu.ill.cameo.api.base.State;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.Requester;


public class TestMultiResponders {

	public static void main(String[] args) {
		
		This.init(args);
		
		String applicationName = null;
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("Responder application is " + applicationName);
			
			if (args.length > 2) {
				numberOfTimes = Integer.parseInt(args[1]);
			}
		}
		else {
			System.err.println("Arguments: [application name]");
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
		
		String[] appArgs = new String[] {args[1]};
		
		// Start the application.
		App responderApplication = server.start(applicationName, appArgs);
		System.out.println("Started application " + responderApplication + " with state " + State.toString(responderApplication.getState()));

		int N = 5;
		
		try {
			Requester[] requesters = new Requester[N];
			
			for (int t = 0; t < N; ++t) {
				
				System.out.println("Creating requester");
				
				requesters[t] = Requester.create(responderApplication, "responder");
				requesters[t].init();
				
				System.out.println("Created requester");
				
			}
			
			Thread[] threads = new Thread[N];
			
			for (int t = 0; t < N; ++t) {
				
				final int ft = t;
				final int fn = numberOfTimes;
				
				threads[ft] = new Thread(new Runnable() {
					public void run() {
					
						for (int i = 0; i < fn; ++i) {
							
							requesters[ft].sendString("" + i);
							System.out.println(ft + " receives " + requesters[ft].receiveString());
						}
					}
				});
				
				threads[ft].start();
			}
			
			for (int t = 0; t < N; ++t) {
				
				try {
					threads[t].join();
				}
				catch (InterruptedException e) {
				}
				
				requesters[t].terminate();
			}
			
			// Stop the responder application.
			responderApplication.stop();
			
			int state = responderApplication.waitFor();
			System.out.println("Responder application terminated with state " + State.toString(state));
			
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}