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

import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.StartException;
import eu.ill.cameo.api.base.This;


public class TestMaxApps {

	public static void main(String[] args) {

		This.init(args);
				
		int numberOfApps = Integer.parseInt(args[0]);
		
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
			// Loop the number of apps.
			for (int i = 0; i < numberOfApps; ++i) {
	
				try {
					// Start the application.
					server.start("maxstopjava");
					
					System.out.println("Started app " + i);
				}
				catch (StartException e) {
					System.out.println(e.getMessage());
				}
			}
			
			server.killAllAndWaitFor("maxstopjava");
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}