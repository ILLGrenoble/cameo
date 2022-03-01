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

import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.PublisherCreationException;

/**
 * The test is not a real test because the subscribers are not synchronized.
 * The test does not finish and the main application must be stopped manually.
 * It can block if the subscribers had not the time to really subscribe (underlying PUB/SUB). 
 * @author legoc
 *
 */
public class TestSubscriber {

	public static void wait(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e) {
		}
	}
	
	public static void main(String[] args) {
		
		This.init(args);
		
		String subscriberApplicationName = null;
		int N = 1;
		if (args.length > 2) {
			subscriberApplicationName = args[0];
			N = Integer.parseInt(args[1]);
			System.out.println("Subscriber application is " + subscriberApplicationName);
		}
		else {
			System.err.println("Arguments: [subscriber application name] [number of loops]");
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
			for (int i = 0; i < N; i++) {
				
				// Create 5 subscribers.
				for (int j = 0; j < 5; ++j) {
					// Pass the name of the application in argument.
					String[] applicationArgs = {This.getName()};
					
					// Start the subscriber applications that can subscribe whereas the publisher is not created.
					Instance subscriberApplication = server.start(subscriberApplicationName, applicationArgs);
					
					if (subscriberApplication.exists()) {
						System.out.println("Started application " + subscriberApplication);
					}
					else {
						System.out.println("Cannot start subscriber application");
					}
				}
				
				// Sleep for 1s to let the subscribers wait.
				wait(1000);
				
				// The publisher is created after the applications that will wait for it.
				fr.ill.ics.cameo.coms.Publisher publisher = fr.ill.ics.cameo.coms.Publisher.create("publisher");
				
				for (int k = 0; k < 20; ++k) {
	
					String data = "{" + k + ", " + k * k + "}";
					publisher.send(data);
					
					System.out.println("sent " + data);
					
					wait(100);
				}		
				
				// The publisher sends end so that the subscriber applications receive end of stream.
				publisher.sendEnd();
				
				// So we must kill all the subscribers.
				List<Instance> subscriberApplications = server.connectAll(subscriberApplicationName);
				for (Instance subscriberApplication : subscriberApplications) {
					subscriberApplication.waitFor();
				}
				
				publisher.terminate();
			}
		}
		catch (PublisherCreationException e) {
			System.out.println("Publisher error");
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}