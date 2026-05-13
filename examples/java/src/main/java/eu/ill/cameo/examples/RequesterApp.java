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

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.State;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.Requester;


public class RequesterApp {

	public static void main(String[] args) {
		
		// Initialize cameo.
		This.init(args);
		
		// Parameters: responder endpoint, language, message, number of times.
		if (args.length < 5) {
			System.out.println("Parameters: <responder endpoint> <language> <message> <number of times>");
			System.exit(-1);
		}
		
		String responderEndpoint = args[0];
		String language = args[1];
		String message = args[2];
		int N = Integer.parseInt(args[3]);
		
		// Initialize the cameo server.
		Server server = Server.create(responderEndpoint);
		server.init();
		
		System.out.println("Connected server " + server);
		
		try {
			// Connect to the responder app.
			String appName = "responder-" + language;
			App responderApp = server.connect(appName);
			
			// Start the responder app if it is not running.
			if (responderApp == null) {
				responderApp = server.start(appName);
			}
						
			System.out.println("App " + responderApp + " has state " + State.toString(responderApp.getState()));
			
			// Create a requester.
			Requester requester = Requester.create(responderApp, "the-responder");
			requester.init();
			System.out.println("Created requester " + requester);
			
			for (int i = 0; i < N; ++i) {
				// Send a simple message as string.
				requester.sendString(message + "-" + i);
				System.out.println("Response is " + requester.receiveString());
			}
			
			// Stop the responder app and wait for its termination.
			responderApp.stop();
			int state = responderApp.waitFor();
			
			System.out.println("App responder finished with state " + State.toString(state));
			
			// Terminate the requester and server.
			requester.terminate();
			server.terminate();
		}
		finally {
			// Do not forget to terminate This.
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}