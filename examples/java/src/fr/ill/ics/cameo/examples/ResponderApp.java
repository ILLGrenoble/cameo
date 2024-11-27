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

import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.basic.Request;
import eu.ill.cameo.api.coms.basic.Responder;


public class ResponderApp {

	public static void main(String[] args) {

		// Initialize cameo.
		This.init(args);
		
		// Define the stop handler to properly stop.
		This.handleStop(() -> {
			This.cancelAll();				
		});
		
		Responder responder = null;
		
		try {
			// Create the responder.
			responder = Responder.create("the-responder");
			responder.init();

			System.out.println("Created and initialized responder " + responder);
			
			// Set the state.
			This.setRunning();

			while (true) {
			
				// Receive the simple request.
				Request request = responder.receive();
				if (request == null) {
					System.out.println("Responder is canceled");
					break;
				}
				
				System.out.println("Received request " + request.getString());
	
				// Reply.
				request.replyString("done");
			}
		}
		finally {
			// Do not forget to terminate This and the responder.
			responder.terminate();
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}