/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.test;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.InitException;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.StartException;
import eu.ill.cameo.api.base.This;


public class TestAppExceptions {

	public static void main(String[] args) {
		
		This.init(args);
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 2) {
			useProxy = Boolean.parseBoolean(args[1]);
		}
		if (useProxy) {
			endpoint = "tcp://localhost:12000";
		}
		
		Server server = Server.create(endpoint, (useProxy ? Option.USE_PROXY : 0));
		server.init();
		
		try {
			try {
				// start the application.
				server.start("fuzz");
			}
			catch (StartException e) {
				System.out.println("Application fuzz cannot be started");
			}
			
			// Connect the application.
			App app = server.connect("fuzz");

			if (app == null) {
				System.out.println("Application fuzz cannot be connected");
			}

			// Test basic responder.
			System.out.println("Creating basic responder");

			eu.ill.cameo.api.coms.basic.Responder basicResponder = eu.ill.cameo.api.coms.basic.Responder.create("basic-responder");
			basicResponder.init();

			System.out.println("Created basic responder");

			eu.ill.cameo.api.coms.basic.Responder basicResponder2 = eu.ill.cameo.api.coms.basic.Responder.create("basic-responder");
			
			try {
				basicResponder2.init();
			}
			catch (InitException e) {
				basicResponder2.terminate();
				System.out.println("Basic responder cannot be created: " + e.getMessage());
			}
			

			// Test multi responder.
			System.out.println("Creating multi responder");

			eu.ill.cameo.api.coms.multi.ResponderRouter multiResponder = eu.ill.cameo.api.coms.multi.ResponderRouter.create("multi-responder");
			multiResponder.init();

			System.out.println("Created multi responder");

			eu.ill.cameo.api.coms.multi.ResponderRouter multiResponder2 = eu.ill.cameo.api.coms.multi.ResponderRouter.create("multi-responder");
			
			try {
				multiResponder2.init();
			}
			catch (InitException e) {
				multiResponder2.terminate();
				System.out.println("Multi responder cannot be created: " + e.getMessage());
			}
			
			
			// Test publisher.
			System.out.println("Creating publisher");

			eu.ill.cameo.api.coms.Publisher publisher = eu.ill.cameo.api.coms.Publisher.create("publisher");
			publisher.init();

			System.out.println("Created publisher");

			eu.ill.cameo.api.coms.Publisher publisher2 = eu.ill.cameo.api.coms.Publisher.create("publisher");
			
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