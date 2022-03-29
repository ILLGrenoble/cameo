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

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.State;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.Subscriber;
import fr.ill.ics.cameo.coms.SubscriberCreationException;

public class SubscriberApp {

	public static JSONObject deserializeToJSON(String jsonMessage) {

		JSONParser parser = new JSONParser();
		JSONObject object = null;
		try {
			object = (JSONObject)parser.parse(jsonMessage);
		} catch (ParseException e) {
			System.err.println("Parse error");
		}
		
		return object;
	}
	
	public static void main(String[] args) {
		
		This.init(args);
					
		// Get the local Cameo server.
		Server server = null;
		
		// The server endpoint is the first argument.
		if (args.length > 1) {
			server = new Server(args[0]);
		}
		else {	
			server = new Server(This.getEndpoint());
		}
		
		if (This.isAvailable() && server.isAvailable()) {
			System.out.println("Connected server " + server);
		} else {
			System.exit(-1);
		}
		
		try {
			// Connect to the publisher application.
			App publisherApp = server.connect("publisher");
			System.out.println("Application " + publisherApp + " has state " + State.toString(publisherApp.getActualState()));
			
			// Create a subscriber to the publisher named "publisher".
			Subscriber subscriber = Subscriber.create(publisherApp, "the-publisher");
			System.out.println("Created subscriber " + subscriber);
			
			// Receive data.
			while (true) {
				String message = subscriber.receiveString();
				if (message != null) {
					System.out.println("Received: " + message);
					
					JSONObject object = deserializeToJSON(message);
					
					System.out.println("\tmessage : " + (String)object.get("message"));
					System.out.println("\tvalue : " + (Long)object.get("value"));
					
				} else {
					break;
				}
			}
				
			// Terminate the subscriber.
			subscriber.terminate();
			
		} catch (SubscriberCreationException e) {
			System.out.println("Cannot create subscriber");
			
		} finally {
			// Do not forget to terminate This.
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}