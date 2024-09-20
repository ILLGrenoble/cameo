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

import eu.ill.cameo.api.base.This;


public class ResponderError {

	public static void main(String[] args) {

		This.init(args);
		
		boolean useProxy = false;
		if (args.length > 1) {
			useProxy = Boolean.parseBoolean(args[0]);
		}
		
		try {
			System.out.println("Creating responder");
			
			// Create the responder.
			eu.ill.cameo.api.coms.basic.Responder responder = eu.ill.cameo.api.coms.basic.Responder.create("responder");
			responder.init();
			
			// Set the state.
			This.setRunning();
			
			
			// Receive the first request.
			eu.ill.cameo.api.coms.basic.Request request = responder.receive();
			System.out.println("Received request " + request.get());

			// Reply.
			request.replyString("response");
			
			// Terminate the responder.
			responder.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
		
		System.exit(123);
	}

}