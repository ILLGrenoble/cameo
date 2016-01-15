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

import java.util.List;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.PublisherCreationException;
import fr.ill.ics.cameo.Server;

/**
 * The test is not a real test because the subscribers are not synchronized.
 * The test does not finish and the main application must be stopped manually.
 * It can block if the subscribers had not the time to really subscribe (underlying PUB/SUB). 
 * @author legoc
 *
 */
public class TestNonSyncSubscribersAndPublisherApplication {

	public static void main(String[] args) {
		
		Application.This.init(args);
		
		String applicationName = null;
		int N = 1;
		if (args.length > 2) {
			applicationName = args[0];
			N = Integer.parseInt(args[1]);
			System.out.println("subscriber application is " + applicationName);
			
		} else {
			System.err.println("arguments: [subscriber application name] [number of subscribers]");
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
			for (int i = 0; i < N; i++) {
				
				// pass the name of the application in argument
				String[] applicationArgs = {Application.This.getName()};
				
				// start the subscriber applications that can subscribe whereas the publisher is not created
				Application.Instance subscriberApplication = server.start(applicationName, applicationArgs);
				
				System.out.println("started application " + subscriberApplication);
			}
		
			// the publisher is created after the applications that will wait for it
			Application.Publisher publisher = Application.Publisher.create("publisher");

			int j = 0;
			while (!Application.This.isStopping()) {

				int[] data = {j, j * j};
				publisher.send(data);
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				
				j++;
			}		
			
			// the publisher must terminate so that the subscriber applications receive end of stream
			publisher.terminate();
			
			// wait for the end of the applications
			List<Application.Instance> subscriberApplications = server.connectAll(applicationName);
			
			for (Application.Instance subscriberApplication : subscriberApplications) {
				subscriberApplication.waitFor();
			}
			
		} catch (ConnectionTimeout e) {
			System.out.println("connection timeout");
			
		} catch (PublisherCreationException e) {
			System.out.println("publisher error");
			
		} finally {
			Application.This.terminate();
		}
		
		System.out.println("finished the application");
	}

}