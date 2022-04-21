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

import fr.ill.ics.cameo.base.This;


public class Publisher {

	public static void main(String[] args) {

		This.init(args);
		
		int numberOfSubscribers = 1;
		if (args.length > 1) {
			numberOfSubscribers = Integer.parseInt(args[0]);
		}
		
		System.out.println("Number of subscribers is " + numberOfSubscribers);
		
		try {
			System.out.println("Creating publisher and waiting for " + numberOfSubscribers + " subscriber(s)...");
			
			// Create the publisher.
			fr.ill.ics.cameo.coms.Publisher publisher = fr.ill.ics.cameo.coms.Publisher.create("publisher", numberOfSubscribers);
			
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