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


public class TestBasicResponder {

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
				
				// Start the application.
				App responderApplication = server.start(applicationName, appArgs);
				System.out.println("Started application " + responderApplication + " with state " + State.toString(responderApplication.getState()));

				Requester requester = Requester.create(responderApplication, "responder");
				requester.setCheckApp(true);
				
				System.out.println("Requester ready ? " + requester.isReady());
				requester.init();
				System.out.println("Requester ready ? " + requester.isReady());
				
				// Try a second init.
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
				
				
				// Send a simple message.
				requester.sendString("request");
				
				// Wait 1s.
				System.out.println("Wait so that the responder has replied");
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
				
				System.out.println("Response is " + requester.receiveString());
				
				
				// Send a simple message.
				requester.sendString("request after wait");
				requester.setTimeout(200);
				
				String response = requester.receiveString();
				
				if (response != null) {
					System.out.println("Response is " + response);
				}
				else if (requester.hasTimedout()) {
					System.out.println("Timeout");	
				}
				else {
					System.out.println("No response");
				}
				

				// The requester needs to resync after a timeout.
				// If the server does not respond within the configured timeout, an error occurs.
				requester.sendString("request after timeout");
				if (requester.hasTimedout()) {
					System.out.println("Timeout while resyncing");
				}
				
				System.out.println("Wait so that the server is able to respond");			
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
				
				// Resend the request.
				requester.sendString("request after timeout");
				if (!requester.hasTimedout()) {
					System.out.println("No timeout while sending");
				}
				
				response = requester.receiveString();
				System.out.println("Response is " + response);
				
				
				// Cancel the requester.
				requester.cancel();
				requester.sendString("request after cancel");
				response = requester.receiveString();

				if (response != null) {
					System.out.println("Response is " + response);
				}
				else {
					if (requester.hasTimedout()) {
						System.out.println("Timeout");
					}
					else if (requester.isCanceled()) {
						System.out.println("Canceled");
					}
				}

				// Re-init the requester has no effect.
				requester.init();
				requester.sendString("2nd request after cancel");
				response = requester.receiveString();

				if (response != null) {
					System.out.println("Response is " + response);
				}
				else {
					if (requester.hasTimedout()) {
						System.out.println("Timeout");
					}
					else if (requester.isCanceled()) {
						System.out.println("Canceled");
					}
				}
				
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