/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.test;

import fr.ill.ics.cameo.api.base.App;
import fr.ill.ics.cameo.api.base.Server;
import fr.ill.ics.cameo.api.base.State;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.base.Timeout;
import fr.ill.ics.cameo.api.coms.Requester;
import fr.ill.ics.cameo.common.messages.Messages;


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
			endpoint = "tcp://localhost:10000";
		}
		
		Server server = Server.create(endpoint, useProxy);
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