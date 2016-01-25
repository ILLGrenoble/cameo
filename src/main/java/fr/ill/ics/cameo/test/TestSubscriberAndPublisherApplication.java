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

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.Server;


public class TestSubscriberAndPublisherApplication {

	public static void main(String[] args) {
		
		Application.This.init(args);
		
		String applicationName = null;
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("publisher application is " + applicationName);
			
		} else {
			System.err.println("arguments: [application name]");
			System.exit(-1);
		}
		
		// get the client services
		Server server = Application.This.getServer();
		
		if (Application.This.isAvailable() && server.isAvailable()) {
			System.out.println("connected application");
			System.out.println("connected server " + server);
		} else {
			System.exit(-1);
		}
		
		try {
			Application.Instance publisherApplication = server.start(applicationName);
			
			System.out.println("started application " + publisherApplication);
			
			Application.Subscriber subscriber = Application.Subscriber.create(publisherApplication, "publisher");
			System.out.println("created subscriber " + subscriber);
			
			// receiving data
			while (true) {
				String data = subscriber.receiveString();
				if (data != null) {
					System.out.println("received " + data);
				} else {
					break;
				}
			}
			
			System.out.println("finished stream, stream ? " + !subscriber.hasEnded());
			
		} catch (ConnectionTimeout e) {
			System.out.println("connection timeout");
			
		} finally {
			// Do not forget to terminate This.
			Application.This.terminate();
		}
		
		System.out.println("finished the application");
	}

}