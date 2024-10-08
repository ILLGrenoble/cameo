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

package eu.ill.cameo.examples;

import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.basic.Request;
import eu.ill.cameo.api.coms.basic.Responder;


public class ResponderApp {

	public static void main(String[] args) {

		// Initialize cameo.
		This.init(args);
		
		// Define the stop handler to properly stop.
		This.handleStop(() -> {
			This.cancelAll();				
		});
		
		Responder responder = null;
		
		try {
			// Create the responder.
			responder = Responder.create("the-responder");
			responder.init();

			System.out.println("Created and initialized responder " + responder);
			
			// Set the state.
			This.setRunning();

			while (true) {
			
				// Receive the simple request.
				Request request = responder.receive();
				if (request == null) {
					System.out.println("Responder is canceled");
					break;
				}
				
				System.out.println("Received request " + request.getString());
	
				// Reply.
				request.replyString("done");
			}
		}
		finally {
			// Do not forget to terminate This and the responder.
			responder.terminate();
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}