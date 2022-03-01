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

import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.This;


public class TestPublisher {

	public static void main(String[] args) {
		
		This.init(args);
		
		String applicationName = null;
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("Publisher application is " + applicationName);
			
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
		
		try {
			// Loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {
			
				// Start the application.
				Instance publisherApplication = server.start(applicationName);
				System.out.println("Started application " + publisherApplication);
				
				fr.ill.ics.cameo.coms.Subscriber subscriber = fr.ill.ics.cameo.coms.Subscriber.create(publisherApplication, "publisher");
				System.out.println("Created subscriber " + subscriber);
				
				// Receiving data.
				while (true) {
					String data = subscriber.receiveString();
					if (data != null) {
						System.out.println("Received " + data);
					} else {
						break;
					}
				}
				
				System.out.println("Finished stream");
				
				// Wait for the application.
				int state = publisherApplication.waitFor();
				System.out.println("Publisher application terminated with state " + Application.State.toString(state));
				
				// Terminate the subscriber.
				subscriber.terminate();
			}	
			
		} finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}