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


public class Subscriber {

	public static void main(String[] args) {
		
		This.init(args);
		
		String applicationName = null;
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("Publisher application is " + applicationName);
			
		} else {
			System.err.println("Arguments: [application name]");
			System.exit(-1);
		}
		
		Server server = This.getServer();
		
		try {
			Instance publisherApplication = server.connect(applicationName);
			if (!publisherApplication.exists()) {
				System.err.println("Publisher error");
				System.exit(-1);
			}
			
			System.out.println("Subscribing publisher...");
			fr.ill.ics.cameo.coms.Subscriber subscriber = fr.ill.ics.cameo.coms.Subscriber.create(publisherApplication, "publisher");
			System.out.println("Synchronized with 1 publisher");
			
			// Receive data.
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
			
		} finally {
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}