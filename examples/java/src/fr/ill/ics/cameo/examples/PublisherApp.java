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

package fr.ill.ics.cameo.examples;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.RemoteException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.Publisher;

public class PublisherApp {

	public static String serializeToJSON(String message, int i) {
		
		JSONObject object = new JSONObject();
		
		object.put("message", message);
		object.put("value", i);
		
		return object.toJSONString();
	}
	
	public static void main(String[] args) {

		This.init(args);
				
		int numberOfSubscribers = 0;
		if (args.length > 1) {
			numberOfSubscribers = Integer.parseInt(args[0]);
		}

		if (This.isAvailable()) {
			System.out.println("Connected");
		}
		
		Publisher publisher = null;
		
		try {
			// Create the publisher not synchronized.
			publisher = Publisher.create("the-publisher", numberOfSubscribers);
			System.out.println("Created publisher " + publisher);
			
			publisher.waitForSubscribers();
			System.out.println("Synchronized with " + numberOfSubscribers + " subscriber(s)");
			
			// Set the state.
			This.setRunning();

			// Loop on the events.
			int i = 0;
			while (true) {
			
				// Send a message.
				publisher.sendString(serializeToJSON("a message", i));
				i++;
				
				// Sleep for 1s.
				Thread.sleep(1000);
			}
						
		} catch (RemoteException e) {
			System.out.println("Publisher error");
			
		} catch (InterruptedException e) {
						
		} finally {
			// Terminate the publisher and This.
			publisher.terminate();
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}