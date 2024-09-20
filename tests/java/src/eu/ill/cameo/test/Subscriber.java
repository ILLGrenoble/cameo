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
import fr.ill.ics.cameo.api.base.Server;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.common.messages.Messages;


public class Subscriber {

	public static void main(String[] args) {
		
		This.init(args);
		
		String applicationName = null;
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("Publisher application is " + applicationName);
		}
		else {
			System.err.println("Arguments: [application name]");
			System.exit(-1);
		}
		
		Server server = This.getServer();
		
		try {
			App publisherApplication = server.connect(applicationName);
			
			System.out.println("Subscribing publisher...");
			fr.ill.ics.cameo.api.coms.Subscriber subscriber = fr.ill.ics.cameo.api.coms.Subscriber.create(publisherApplication, "publisher");
			subscriber.init();
			System.out.println("Synchronized with 1 publisher");
			
			// Receive data.
			while (true) {
				byte[][] data = subscriber.receiveTwoParts();
				if (data != null) {
					System.out.println("Received " + Messages.parseString(data[0]) + ", " + Messages.parseString(data[1]));
				}
				else {
					break;
				}
			}
			
			System.out.println("Finished stream");
		}
		catch (InitException e) {
			System.out.println("Cannot create subscriber");
		}
		finally {
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}