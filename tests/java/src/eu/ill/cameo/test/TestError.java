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

import java.util.Date;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.State;
import eu.ill.cameo.api.base.This;


public class TestError {

	public static void main(String[] args) {
		
		This.init(args);
		
		int numberOfTimes = 1;
		
		if (args.length > 1) {
			numberOfTimes = Integer.parseInt(args[0]);
		}
		
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
		
		Date d = new Date();
		
		try {
			// loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {
			
				// Test waitFor.
				{
					// start the application.
					App application = server.start("errorjava");
		
					// the getString is blocking until the application finishes
					int state = application.waitFor();
									
					System.out.println("Finished application " + application + " with state " + State.toString(state) + " with exit code " + application.getExitCode());
				}
				
				// Test getLastState.
				{
					// start the application.
					App application = server.start("errorjava");
		
					while (application.getLastState() != State.FAILURE) {
						try {
							Thread.sleep(100);
						}
						catch (InterruptedException e) {
						}
						
						System.out.println("...checking application state");
					}
					
					// the getString is blocking until the application finishes
					int state = application.waitFor();
									
					System.out.println("Finished application " + application + " with state " + State.toString(state) + " with exit code " + application.getExitCode());
				}
				
				// Test getActualState.
				{
					// start the application.
					App application = server.start("errorjava");
		
					while (application.getState() != State.NIL) {
						try {
							Thread.sleep(100);
						}
						catch (InterruptedException e) {
						}
						
						System.out.println("...checking application state");
					}
					
					// the getString is blocking until the application finishes
					int state = application.waitFor();
									
					System.out.println("Finished application " + application + " with state " + State.toString(state) + " with exit code " + application.getExitCode());
				}
			}
		}
		finally {
			// do not forget to terminate the server and application
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application after " + ((new Date()).getTime() - d.getTime()) + "ms");
	}

}