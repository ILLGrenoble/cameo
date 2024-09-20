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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.api.base.App;
import fr.ill.ics.cameo.api.base.Server;
import fr.ill.ics.cameo.api.base.State;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.coms.Subscriber;

public class SubscriberApp {

	public static JSONObject deserializeToJSON(String jsonMessage) {

		JSONParser parser = new JSONParser();
		JSONObject object = null;
		try {
			object = (JSONObject)parser.parse(jsonMessage);
		}
		catch (ParseException e) {
			System.err.println("Parse error");
		}
		
		return object;
	}
	
	public static void main(String[] args) {
		
		// Initialize cameo.
		This.init(args);
		
		// Parameters: publisher endpoint, language.
		if (args.length < 3) {
			System.out.println("Parameters: <publisher endpoint> <language>");
			System.exit(-1);
		}
		
		// Define the stop handler to properly stop.
		This.handleStop(() -> {
			// Cancel the subscriber.
			This.cancelAll();
		});
		
		String publisherEndpoint = args[0];
		String language = args[1];
		
		// Initialize the cameo server.
		Server server = Server.create(publisherEndpoint);
		server.init();
		
		try {
			// Connect to the publisher app.
			String appName = "publisher-" + language;
			App publisherApp = server.connect(appName);
			
			// Start the publisher app if it is not running.
			if (publisherApp == null) {
				publisherApp = server.start(appName);
			}
						
			System.out.println("App " + publisherApp + " has state " + State.toString(publisherApp.getState()));
			
			// Create a subscriber.
			Subscriber subscriber = Subscriber.create(publisherApp, "the-publisher");
			
			subscriber.init();
			
			System.out.println("Created subscriber " + subscriber);
			
			// Receive messages as long as the subscriber has not been canceled.
			while (true) {
				String message = subscriber.receiveString();
				if (message != null) {
					System.out.println("Received: " + message);
					
					JSONObject object = deserializeToJSON(message);
					
					System.out.println("\tmessage: " + (String)object.get("message"));
					System.out.println("\tvalue: " + (Long)object.get("value"));
				}
				else {
					// Subscriber is canceled.
					break;
				}
			}
			
			// Stop the publisher app and wait for its termination.
			publisherApp.stop();
			int state = publisherApp.waitFor();
						
			System.out.println("App publisher finished with state " + State.toString(state));
				
			// Terminate the subscriber and server.
			subscriber.terminate();
			server.terminate();
		}
		finally {
			// Do not forget to terminate This.
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}