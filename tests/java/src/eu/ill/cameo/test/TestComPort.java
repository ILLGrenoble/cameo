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
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.This;


public class TestComPort {

	public static void main(String[] args) {
		
		This.init(args);
		
		int numberOfTimes = 1;
		if (args.length > 1) {
			numberOfTimes = Integer.parseInt(args[0]);
		}

		Server server = This.getServer();
		
		try {
			int port = This.getCom().requestPort();
			System.out.println("Received port " + port);
						
			// Loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {
			
				// Start the application.
				App application = server.start("comportjava");
	
				application.waitFor();
				System.out.println("Finished application " + application);
			}
			
			port = This.getCom().requestPort();
			System.out.println("Received port " + port + " that must be greater than first port");
		}
		finally {
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}