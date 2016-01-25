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
import fr.ill.ics.cameo.RemoteException;


public class TestPublisherLoopApplication {

	public static void main(String[] args) {

		Application.This.init(args);
				
		if (Application.This.isAvailable()) {
			System.out.println("connected");
		}
		
		try {
			System.out.println("creating publisher and waiting for 1 subscriber...");
			
			// create the publisher
			Application.Publisher publisher = Application.Publisher.create("publisher", 1);
			
			// synchronize with subscribers
			publisher.waitForSubscribers();
			
			System.out.println("synchronized with the subscriber");
			
			Application.This.setRunning();
			
			// sending data
			while (true) {
				publisher.send("hello");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			
		} catch (RemoteException e) {
			System.out.println("publisher error");
			
		} finally {
			// Do not forget to terminate This.
			Application.This.terminate();			
		}
		
		System.out.println("finished the application");
	}

}