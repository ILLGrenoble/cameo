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

import fr.ill.ics.cameo.api.base.ServerAndApp;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.coms.Requester;


public class BasicRequester {

	public static void main(String[] args) {

		This.init(args);
		
		boolean useProxy = false;
		if (args.length > 1) {
			useProxy = Boolean.parseBoolean(args[0]);
		}
		
		try {
			System.out.println("Creating requester");
			
			// Get the starter app.
			ServerAndApp starter = This.connectToStarter(0, useProxy);
			
			// Create the requester.
			Requester requester = Requester.create(starter.getApp(), "responder");
			requester.init();
			
			// Set the state.
			This.setRunning();

			// Send 10 requests.
			int R = 10;
			for (int i = 0; i < R; ++i) {
				// Send and wait for the result.
				requester.sendString("test");
				String result = requester.receiveString();
				
				System.out.println("Received " + result);
			}
			
			// Terminate the requester.
			requester.terminate();
			starter.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}