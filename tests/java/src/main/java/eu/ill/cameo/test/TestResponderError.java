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


public class TestResponderError {

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
		
		try {
			
			// Loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {
			
				// Args.
				String[] appArgs = new String[] {args[2]};
				
				// Test with check app = false
				{
					// Start the application.
					App responderApplication = server.start(applicationName, appArgs);
					System.out.println("Started application " + responderApplication + " with state " + State.toString(responderApplication.getState()));
	
					Requester requester = Requester.create(responderApplication, "responder");
					requester.setCheckApp(false);
					requester.setTimeout(1000);
					requester.init();
					
					// Check the state of the responder app.
					System.out.println("Application " + responderApplication + " has state " + State.toString(responderApplication.getState()));
					
					// Send a simple message.
					requester.sendString("request");
					System.out.println("Response is " + requester.receiveString());
	
					for (int j = 0; j < 3; j++) {

						// Re-send message.
						requester.sendString("request");
						byte[] response = requester.receive();
		
						if (response == null) {
							System.out.println("No response");
						}
						
						if (requester.hasTimedout()) {
							System.out.println("Timeout");
						}
						
						if (requester.isCanceled()) {
							System.out.println("Canceled");
						}
					}
					
					int state = responderApplication.waitFor();
					
					System.out.println("Application " + responderApplication + " terminated with state " + State.toString(state));
					
					// Terminate the requester.
					requester.terminate();
				}
				
				// Test with check app = true so that the requester must be canceled.
				{
					// Start the application.
					App responderApplication = server.start(applicationName, appArgs);
					System.out.println("Started application " + responderApplication + " with state " + State.toString(responderApplication.getState()));
	
					Requester requester = Requester.create(responderApplication, "responder");
					requester.setCheckApp(true);
					requester.init();
					
					// Check the state of the responder app.
					System.out.println("Application " + responderApplication + " has state " + State.toString(responderApplication.getState()));
					
					// Send a simple message.
					requester.sendString("request");
					System.out.println("Response is " + requester.receiveString());
					
					for (int j = 0; j < 3; j++) {

						// Re-send message.
						requester.sendString("request");
						byte[] response = requester.receive();
		
						if (response == null) {
							System.out.println("No response");
						}
						
						if (requester.hasTimedout()) {
							System.out.println("Timeout");
						}
						
						if (requester.isCanceled()) {
							System.out.println("Canceled");
						}
					}
					
					int state = responderApplication.waitFor();
					
					System.out.println("Application " + responderApplication + " terminated with state " + State.toString(state));
					
					// Terminate the requester.
					requester.terminate();
				}
			}
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}