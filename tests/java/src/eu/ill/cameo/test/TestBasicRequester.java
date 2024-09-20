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

package eu.ill.cameo.test;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.This;


public class TestBasicRequester {

	public static void main(String[] args) {
		
		This.init(args);
		
		String applicationName = null;
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("Requester application is " + applicationName);
			
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
			// Set the state.
			This.setRunning();
			
			int N = 5;
			
			// loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {

				App[] requesterApps = new App[N];
				
				// Start the requester applications.
				for (int j = 0; j < N; ++j) {
					
					// Args.
					String[] appArgs = new String[] {args[2]};
					
					// Start the application.
					requesterApps[j] = server.start(applicationName, appArgs);
					System.out.println("Started application " + requesterApps[j]);
				}
				
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
				
				// Create the responder.
				eu.ill.cameo.api.coms.basic.Responder responder = eu.ill.cameo.api.coms.basic.Responder.create("responder");
				
				System.out.println("Responder ready ? " + responder.isReady());
				responder.init();
				System.out.println("Responder ready ? " + responder.isReady());
				
				// Try a second init.
				responder.init();
				
				// Process the requests, the requester application sends 10 requests.
				for (int j = 0; j < N * 10; ++j) {
					
					// Receive the simple request.
					eu.ill.cameo.api.coms.basic.Request request = responder.receive();
		    		request.replyString("done");
		    		
		    		System.out.println("Processed " + request);
				}
				
				// Wait for the requester applications.
				for (int j = 0; j < N; ++j) {
					requesterApps[j].waitFor();
					System.out.println("Finished application " + requesterApps[j]);
				}
				
				responder.terminate();
			}
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}