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


public class TestStop {

	public static void main(String[] args) {

		This.init(args);
		
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			numberOfTimes = Integer.parseInt(args[0]);
		}
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 2) {
			useProxy = Boolean.parseBoolean(args[1]);
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
				App stopApplication = server.start("stopjava");
	
				System.out.println("Waiting 0.1s...");
				
				// Stop the application.
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}
				
				System.out.println("Stopping application " + stopApplication.getNameId());
				stopApplication.stop();
				
				// The call is blocking until the application finishes.
				String result = stopApplication.getStringResult();
				
				if (result != null) {
					System.out.println("Stop application returned " + result);
				}
				else {
					System.out.println("Stop application has no result");
				}
				
				System.out.println("Stop application finished");
			}
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}