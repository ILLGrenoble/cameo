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
import eu.ill.cameo.common.messages.Messages;


public class MultiResponder {

	public static void main(String[] args) {

		This.init(args);
		
		try {
			System.out.println("Creating router");
			
			// Create the router.
			eu.ill.cameo.api.coms.multi.ResponderRouter router = eu.ill.cameo.api.coms.multi.ResponderRouter.create("responder");
			router.init();

			System.out.println("Created router");
			
			// Set the state.
			This.setRunning();
			
			Thread thread = new Thread(new Runnable() {
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
					
					eu.ill.cameo.api.coms.multi.Request request = responder.receive();
					System.out.println("Received request " + request.get());

					// Reply.
					request.replyString("1st response");
					request.replyString("1st response (bis)");
					
					request = responder.receive();
					byte[][] data = request.getTwoParts();
					System.out.println("Received request " + Messages.parseString(data[0]) + " " + Messages.parseString(data[1]));

					// Reply.
					request.replyString("2nd response");

					router.cancel();
					
					responder.terminate();
				}
			});
			
			thread.start();
			
			router.run();
			
			try {
				thread.join();
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