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
import fr.ill.ics.cameo.api.base.InitException;
import fr.ill.ics.cameo.api.base.Option;
import fr.ill.ics.cameo.api.base.Server;
import fr.ill.ics.cameo.api.base.State;
import fr.ill.ics.cameo.api.base.This;


public class TestPublisherError {

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
			endpoint = "tcp://localhost:12000";
		}
		
		Server server = Server.create(endpoint, (useProxy ? Option.USE_PROXY : 0));
		server.init();
		
		try {
			// Loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {
			
				// Start the application.
				App publisherApplication = server.start(applicationName);
				System.out.println("Started application " + publisherApplication);
				
				fr.ill.ics.cameo.api.coms.Subscriber subscriber = fr.ill.ics.cameo.api.coms.Subscriber.create(publisherApplication, "publisher");
				subscriber.setCheckApp(true);
				subscriber.init();
				
				System.out.println("Created subscriber " + subscriber);
				
				// Receiving data.
				while (true) {
					String data = subscriber.receiveString();
					if (data != null) {
						System.out.println("Received " + data);
					}
					else {
						break;
					}
				}
				
				System.out.println("Finished stream");
				
				// Wait for the application.
				int state = publisherApplication.waitFor();
				System.out.println("Publisher application terminated with state " + State.toString(state));
				
				// Terminate the subscriber.
				subscriber.terminate();
			}
		}
		catch (InitException e) {
			System.out.println("Cannot create subscriber");
			
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}