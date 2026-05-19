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
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.State;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.Requester;
import eu.ill.cameo.common.messages.Messages;


public class TestHeartbeat {

	public static void main(String[] args) {

		This.init(args);
		
		String applicationName = null;
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("Responder application is " + applicationName);
			
			if (args.length > 2) {
				numberOfTimes = Integer.parseInt(args[1]);
			}
		}
		else {
			System.err.println("Arguments: [application name]");
			System.exit(-1);
		}
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 3) {
			useProxy = Boolean.parseBoolean(args[2]);
		}
		if (useProxy) {
			endpoint = "tcp://localhost:12000";
		}
		
		Server server = Server.create(endpoint, (useProxy ? Option.USE_PROXY : 0));
		server.init();
		
		try {
			This.heartbeat(1, 1);
			
			// Args.
			String[] appArgs = new String[] {args[2]};
			
			// Start the application.
			App heartbeatApplication = server.start(applicationName, appArgs);
			
			Requester requester = Requester.create(heartbeatApplication, "responder");
			requester.init();
			
			System.out.println("Requester ready ? " + requester.isReady());
			
			eu.ill.cameo.api.coms.Publisher publisher = eu.ill.cameo.api.coms.Publisher.create("publisher");
			publisher.init();

			System.out.println("Publisher ready ? " + publisher.isReady());
			
			try {
				int N = 5;
				for (int i = 0; i < N; i++) {
					System.out.println("" + (i + 1) + " / " + N);
					Thread.sleep(1000);
				}
			}
			catch (InterruptedException e) {
			}
			
			System.out.println("Waiting for the application");
			
			heartbeatApplication.waitFor();
			
			System.out.println("Application terminated");
			
			requester.terminate();
			publisher.terminate();
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}