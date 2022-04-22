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


public class PublisherLoop {

	public static void main(String[] args) {

		This.init(args);
				
		try {
			System.out.println("Creating publisher and waiting for 1 subscriber...");
			
			// Create the publisher.
			fr.ill.ics.cameo.coms.Publisher publisher = fr.ill.ics.cameo.coms.Publisher.create("publisher", 1);
			
			// Synchronize with subscribers.
			publisher.init();
			
			System.out.println("Synchronized with the subscriber");
			
			This.setRunning();
			
			// Sending data.
			while (!publisher.hasEnded()) {
				
				publisher.sendString("hello");
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}