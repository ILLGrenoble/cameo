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
import eu.ill.cameo.common.messages.Messages;


public class TestMultiResponder {

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
			
				// Start the application.
				App responderApplication = server.start(applicationName);
				System.out.println("Started application " + responderApplication + " with state " + State.toString(responderApplication.getState()));

				Requester requester = Requester.create(responderApplication, "responder");
				requester.init();
				System.out.println("Created requester " + requester);
			
				// Check the state of the responder app.
				System.out.println("Application " + responderApplication + " has state " + State.toString(responderApplication.getState()));
				
				
				// Send a simple message.
				requester.sendString("request");
				System.out.println("Response is " + requester.receiveString());
				System.out.println("Response 2 is " + requester.receiveString());
			
				
				// Send a two-parts message.
				requester.sendTwoParts(Messages.serialize("first"), Messages.serialize("second"));
				System.out.println("Response is " + requester.receiveString());
				
				int state = responderApplication.waitFor();
				System.out.println("Responder application terminated with state " + State.toString(state));			
				
				// Terminate the requester.
				requester.terminate();
			}
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}