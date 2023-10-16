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
import fr.ill.ics.cameo.api.base.Option;
import fr.ill.ics.cameo.api.base.Server;
import fr.ill.ics.cameo.api.base.State;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.coms.Requester;


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
			endpoint = "tcp://localhost:10000";
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
				requester.init();
				
				System.out.println("Created requester " + requester);
			
				// Check the state of the responder app.
				System.out.println("Application " + responderApplication + " has state " + State.toString(responderApplication.getState()));
				
				// Send a simple message.
				requester.sendString("request");
				System.out.println("Response is " + requester.receiveString());

				byte[] response = requester.receive();

				if (response == null) {
					System.out.println("No response");
				}
				
				if (requester.isCanceled()) {
					System.out.println("Requester canceled");
				}
				
				int state = responderApplication.waitFor();
				
				System.out.println("Application " + responderApplication + " terminated with state " + State.toString(state));
				
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