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

import eu.ill.cameo.api.base.This;


public class Publisher {

	public static void main(String[] args) {

		This.init(args);
		
		boolean syncSubscribers = false;
		int numberOfSubscribers = 1;
		
		if (args.length > 1) {
			syncSubscribers = (args[0].equals("true"));
		}
		
		if (args.length > 2) {
			numberOfSubscribers = Integer.parseInt(args[1]);
		}
		
		System.out.println("Number of subscribers is " + numberOfSubscribers);
		System.out.println("Sync subscribers is " + syncSubscribers);
		
		try {
			System.out.println("Creating publisher and waiting for " + numberOfSubscribers + " subscriber(s)...");
			
			// Create the publisher.
			eu.ill.cameo.api.coms.Publisher publisher = eu.ill.cameo.api.coms.Publisher.create("publisher");
			publisher.setSyncSubscribers(syncSubscribers);
			publisher.setWaitForSubscribers(numberOfSubscribers);
			
			// Synchronize with subscribers.
			publisher.init();
			
			System.out.println("Synchronized with the subscriber(s)");
			
			This.setRunning();
			
			// Sending data.
			for (int i = 0; i < 100; ++i) {
				publisher.sendString("message " + i);
			}
			
			publisher.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}