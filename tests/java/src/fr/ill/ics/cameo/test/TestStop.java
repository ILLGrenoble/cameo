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
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.This;


public class TestStop {

	public static void main(String[] args) {

		This.init(args);
		Server server = This.getServer();
		
		int numberOfTimes = 1;
		if (args.length > 1) {
			numberOfTimes = Integer.parseInt(args[0]);
		}
		
		try {
			// Loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {
				
				// Start the application.
				Instance stopApplication = server.start("stopjava");
	
				System.out.println("Waiting 100ms...");
				
				// Stop the application after 1s.
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				
				System.out.println("Stopping application " + stopApplication.getNameId());
				stopApplication.stop();
				
				// The call is blocking until the application finishes.
				String result = stopApplication.getStringResult();
				
				if (result != null) {
					System.out.println("Stop application returned " + result);

				} else {
					System.out.println("Stop application has no result");
				}
				
				System.out.println("Stop application finished");
			}
						
		} finally {
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}