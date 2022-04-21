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

import fr.ill.ics.cameo.base.AppException;
import fr.ill.ics.cameo.base.InitException;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.This;


public class TestAppExceptions {

	public static void main(String[] args) {
		
		This.init(args);
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 2) {
			useProxy = Boolean.parseBoolean(args[1]);
		}
		if (useProxy) {
			endpoint = "tcp://localhost:10000";
		}
		
		Server server = Server.create(endpoint, useProxy);
		server.init();
		
		try {
			try {
				// start the application.
				server.start("fuzz");
			}
			catch (AppException e) {
				System.out.println("Application fuzz cannot be started");
			}
			
			try {
				// Connect the application.
				server.connect("fuzz");
			}
			catch (AppException e) {
				System.out.println("Application fuzz cannot be connected");
			}

			// Test basic responder.
			System.out.println("Creating basic responder");

			fr.ill.ics.cameo.coms.basic.Responder basicResponder = fr.ill.ics.cameo.coms.basic.Responder.create("basic-responder");
			basicResponder.init();

			System.out.println("Created basic responder");

			fr.ill.ics.cameo.coms.basic.Responder basicResponder2 = fr.ill.ics.cameo.coms.basic.Responder.create("basic-responder");
			
			try {
				basicResponder2.init();
			}
			catch (InitException e) {
				basicResponder2.terminate();
				System.out.println("Basic responder cannot be created: " + e.getMessage());
			}
			

			// Test multi responder.
			System.out.println("Creating multi responder");

			fr.ill.ics.cameo.coms.multi.ResponderRouter multiResponder = fr.ill.ics.cameo.coms.multi.ResponderRouter.create("multi-responder");
			multiResponder.init();

			System.out.println("Created multi responder");

			fr.ill.ics.cameo.coms.multi.ResponderRouter multiResponder2 = fr.ill.ics.cameo.coms.multi.ResponderRouter.create("multi-responder");
			
			try {
				multiResponder2.init();
			}
			catch (InitException e) {
				multiResponder2.terminate();
				System.out.println("Multi responder cannot be created: " + e.getMessage());
			}
			
			
			// Test publisher.
			System.out.println("Creating publisher");

			fr.ill.ics.cameo.coms.Publisher publisher = fr.ill.ics.cameo.coms.Publisher.create("publisher");
			publisher.init();

			System.out.println("Created publisher");

			fr.ill.ics.cameo.coms.Publisher publisher2 = fr.ill.ics.cameo.coms.Publisher.create("publisher");
			
			try {
				publisher2.init();
			}
			catch (InitException e) {
				publisher2.terminate();
				System.out.println("Publisher cannot be created: " + e.getMessage());
			}
			
		}
		finally {
			// Do not forget to terminate the server and application.
			server.terminate();
			This.terminate();
		}
	}

}