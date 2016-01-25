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


public class TestPublisherApplication {

	public static void main(String[] args) {

		Application.This.init(args);
		
		int numberOfSubscribers = 1;
		if (args.length > 1) {
			numberOfSubscribers = Integer.parseInt(args[0]);
		}
		
		System.out.println("number of subscribers is " + numberOfSubscribers);
		
		if (Application.This.isAvailable()) {
			System.out.println("connected");
		}
		
		try {
			System.out.println("creating publisher and waiting for " + numberOfSubscribers + " subscriber(s)...");
			
			// create the publisher
			Application.Publisher publisher = Application.Publisher.create("publisher", numberOfSubscribers);
			
			// synchronize with subscribers
			publisher.waitForSubscribers();
			
			System.out.println("synchronized with the subscriber(s)");
			
			Application.This.setRunning();
			
			// sending data
			publisher.send("hello");
			publisher.send("world");
			publisher.send("!");
			
		} catch (RemoteException e) {
			System.out.println("publisher error");
			
		} finally {
			// Do not forget to terminate This.
			Application.This.terminate();			
		}
		
		System.out.println("finished the application");
	}

}