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

import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.RemoteException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.Request;
import fr.ill.ics.cameo.coms.Responder;


public class ResponderApp {

	public static void main(String[] args) {

		This.init(args);
		
		if (This.isAvailable()) {
			System.out.println("Connected");
		}
				
		This.handleStop(new Application.Handler() {
			
			@Override
			public void handle() {
				This.cancelWaitings();				
			}
		});
		
		Responder responder = null;
		
		try {
			// Create the responder.
			responder = Responder.create("the-responder");

			System.out.println("Created responder " + responder);
			
			// Set the state.
			This.setRunning();

			// Loop on the requests.
			while (true) {
			
				// Receive the simple request.
				Request request = responder.receive();
				if (request == null) {
					System.out.println("Responder canceled");
					break;
				}
				
				System.out.println("Received request " + request.get());
	
				// Reply.
				request.reply("done");
				
				// Terminate the request object.
				request.terminate();
			}
						
		} catch (RemoteException e) {
			System.out.println("Responder error");
			
		} finally {
			// Do not forget to terminate This and the responder.
			responder.terminate();
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}