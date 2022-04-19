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
import fr.ill.ics.cameo.coms.ResponderCreateException;
import fr.ill.ics.cameo.messages.Messages;


public class MultiResponder {

	public static void main(String[] args) {

		This.init(args);
		
		try {
			System.out.println("Creating router");
			
			// Create the router.
			fr.ill.ics.cameo.coms.multi.ResponderRouter router = fr.ill.ics.cameo.coms.multi.ResponderRouter.create("responder");
			router.init();

			System.out.println("Created router");
			
			// Set the state.
			This.setRunning();
			
			Thread thread = new Thread(new Runnable() {
				public void run() {

					// Create the responder.
					fr.ill.ics.cameo.coms.multi.Responder responder = null;
					
					try {
						System.out.println("Creating responder");
						
						responder = fr.ill.ics.cameo.coms.multi.Responder.create(router);
						responder.init();
						
						System.out.println("Created responder");
					}
					catch (ResponderCreateException e) {
					}
					
					fr.ill.ics.cameo.coms.multi.Request request = responder.receive();
					System.out.println("Received request " + request.get());

					// Reply.
					request.replyString("1st response");
					
					request = responder.receive();
					byte[][] data = request.getTwoParts();
					System.out.println("Received request " + Messages.parseString(data[0]) + " " + Messages.parseString(data[1]));

					// Reply.
					request.replyString("2nd response");

					router.cancel();
					
					responder.terminate();
				}
			});
			
			thread.start();
			
			router.run();
			
			try {
				thread.join();
			}
			catch (InterruptedException e) {
			}
			
			// Terminate the router.
			router.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}