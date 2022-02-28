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

import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.RemoteException;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.This;


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
			
		} else {
			System.err.println("Arguments: [application name]");
			System.exit(-1);
		}
		
		//Server server = This.getServer();
		Server server = new Server("tcp://localhost:10000", 0, true);
		//Server server = new Server("tcp://localhost:11000", 0, false);
		
		try {
			// Set the state.
			This.setRunning();
			
			int N = 5;
			
			// loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {

				Instance[] requesterApps = new Instance[N];
				
				// Start the requester applications.
				for (int j = 0; j < N; ++j) {
					
					// Start the application.
					requesterApps[j] = server.start(applicationName);
					System.out.println("Started application " + requesterApps[j]);
				}
				
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
				
				// Create the responder.
				fr.ill.ics.cameo.coms.basic.Responder responder = fr.ill.ics.cameo.coms.basic.Responder.create("responder");
				
				// Process the requests, the requester application sends 10 requests.
				for (int j = 0; j < N * 10; ++j) {
					
					// Receive the simple request.
					fr.ill.ics.cameo.coms.basic.Request request = responder.receive();
		    		request.reply("done");
		    		
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
		catch (RemoteException e) {
			System.out.println("Requester error:" + e);
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}