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

import fr.ill.ics.cameo.base.InitException;
import fr.ill.ics.cameo.base.This;


public class MultiResponders {

	public static void main(String[] args) {

		This.init(args);
		
		This.handleStop(() -> {
			This.cancelAll();
			
			System.out.println("Stopped");
		});
		
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			numberOfTimes = Integer.parseInt(args[0]);
		}
		
		try {
			System.out.println("Creating router");
			
			// Create the router.
			fr.ill.ics.cameo.coms.multi.ResponderRouter router = fr.ill.ics.cameo.coms.multi.ResponderRouter.create("responder");
			router.init();

			System.out.println("Created router");
			
			// Set the state.
			This.setRunning();
			
			int N = 5;
			
			Thread[] threads = new Thread[N];
			
			for (int t = 0; t < N; ++t) {
	
				final int ft = t;
				final int fn = numberOfTimes;
				
				threads[ft] = new Thread(new Runnable() {
					public void run() {
	
						// Create the responder.
						fr.ill.ics.cameo.coms.multi.Responder responder = null;
						
						try {
							System.out.println("Creating responder");
							
							responder = fr.ill.ics.cameo.coms.multi.Responder.create(router);
							responder.init();
							
							System.out.println("Created responder");
						}
						catch (InitException e) {
						}
	
						for (int i = 0; i < fn; ++i) {
						
							fr.ill.ics.cameo.coms.multi.Request request = responder.receive();
							System.out.println("Received request " + request.get());
							
							request.replyString(ft + " to " + request.getString());
						}
						
						responder.terminate();
					}
				});
				
				threads[t].start();
			}
			
			System.out.println("Router runs");
			
			router.run();
			
			System.out.println("Router stopped");
			
			try {
				for (int t = 0; t < N; ++t) {
					threads[t].join();
				}
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