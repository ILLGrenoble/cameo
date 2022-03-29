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

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.RemoteException;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.State;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.Requester;


public class TestMultiResponders {

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
		
		Server server = new Server(endpoint, 0, useProxy);

		String[] appArgs = new String[] {args[1]};
		
		// Start the application.
		App responderApplication = server.start(applicationName, appArgs);
		System.out.println("Started application " + responderApplication + " with state " + State.toString(responderApplication.getActualState()));

		int N = 5;
		
		try {
			Requester[] requesters = new Requester[N];
			
			for (int t = 0; t < N; ++t) {
				
				System.out.println("Creating requester");
				
				requesters[t] = Requester.create(responderApplication, "responder");
				
				System.out.println("Created requester");
				
			}
			
			Thread[] threads = new Thread[N];
			
			for (int t = 0; t < N; ++t) {
				
				final int ft = t;
				final int fn = numberOfTimes;
				
				threads[ft] = new Thread(new Runnable() {
					public void run() {
					
						for (int i = 0; i < fn; ++i) {
							
							requesters[ft].send("" + i);
							System.out.println(ft + " receives " + requesters[ft].receiveString());
						}
					}
				});
				
				threads[ft].start();
			}
			
			for (int t = 0; t < N; ++t) {
				
				try {
					threads[t].join();
				}
				catch (InterruptedException e) {
				}
				
				requesters[t].terminate();
			}
			
			// Stop the responder application.
			responderApplication.stop();
			
			int state = responderApplication.waitFor();
			System.out.println("Responder application terminated with state " + State.toString(state));
			
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