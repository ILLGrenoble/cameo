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

import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.ServerAndApp;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.Requester;
import eu.ill.cameo.api.coms.Subscriber;


public class ComsTimeout {

	public static void main(String[] args) {

		This.init(args);
		
		boolean useProxy = false;
		if (args.length > 1) {
			useProxy = Boolean.parseBoolean(args[0]);
		}
		
		try {
			// Get the starter app.
			ServerAndApp starter = This.connectToStarter((useProxy ? Option.USE_PROXY : 0));

			System.out.println("Connected to starter");
		
			{
				// Create the requester.
				Requester requester = Requester.create(starter.getApp(), "an unknown responder");
				requester.setTimeout(500);
				
				// Set the state.
				This.setRunning();
	
				try {
					requester.init();
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println("Requester ready ? " + requester.isReady());
				}
				
				// Terminate the requester.
				requester.terminate();
			}
			
			{
				// Create the subscriber.
				Subscriber subscriber = Subscriber.create(starter.getApp(), "an unknown publisher");
				subscriber.setTimeout(500);
	
				try {
					subscriber.init();
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println("Subscriber ready ? " + subscriber.isReady());
				}
				
				subscriber.terminate();			
			}
			
			{
				// Create the subscriber.
				Subscriber subscriber = Subscriber.create(starter.getApp(), "pub");
				subscriber.setTimeout(500);
	
				try {
					subscriber.init();
				}
				catch (Exception e) {
					System.out.println(e.getMessage());
					System.out.println("Subscriber ready ? " + subscriber.isReady());
				}
				
				subscriber.terminate();			
			}	
			
			starter.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}