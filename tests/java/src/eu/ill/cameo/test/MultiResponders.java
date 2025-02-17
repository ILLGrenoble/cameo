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

import eu.ill.cameo.api.base.InitException;
import eu.ill.cameo.api.base.This;


public class MultiResponders {

	public static void main(String[] args) {

		This.init(args);
		
		This.handleStop(() -> {
			This.cancelAll();
			
			System.out.println("Stopped");
		});
		
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			numberOfTimes = Integer.parseInt(args[0]);
		}
		
		try {
			System.out.println("Creating router");
			
			// Create the router.
			eu.ill.cameo.api.coms.multi.ResponderRouter router = eu.ill.cameo.api.coms.multi.ResponderRouter.create("responder");
			router.init();

			System.out.println("Created router");
			
			// Set the state.
			This.setRunning();
			
			int N = 5;
			
			Thread[] threads = new Thread[N];
			
			for (int t = 0; t < N; ++t) {
	
				final int ft = t;
				final int fn = numberOfTimes;
				
				threads[ft] = new Thread(new Runnable() {
					public void run() {
	
						// Create the responder.
						eu.ill.cameo.api.coms.multi.Responder responder = null;
						
						try {
							System.out.println("Creating responder");
							
							responder = eu.ill.cameo.api.coms.multi.Responder.create(router);
							responder.init();
							
							System.out.println("Created responder");
						}
						catch (InitException e) {
						}
	
						for (int i = 0; i < fn; ++i) {
						
							eu.ill.cameo.api.coms.multi.Request request = responder.receive();
							System.out.println("Received request " + request.get());
							
							request.replyString(ft + " to " + request.getString());
						}
						
						responder.terminate();
					}
				});
				
				threads[t].start();
			}
			
			System.out.println("Router runs");
			
			router.run();
			
			System.out.println("Router stopped");
			
			try {
				for (int t = 0; t < N; ++t) {
					threads[t].join();
				}
			}
			catch (InterruptedException e) {
			}
			
			// Terminate the router.
			router.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}