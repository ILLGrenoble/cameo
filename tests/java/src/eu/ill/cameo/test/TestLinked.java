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


public class TestLinked {

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
		
		// Loop the number of times.
		for (int i = 0; i < numberOfTimes; ++i) {
			
			{
				App app = server.start("linkedjava");
				app.waitFor(State.RUNNING);
				App stopApp = server.connect("stopjava");
				stopApp.waitFor(State.RUNNING);
				
				app.kill();
				app.waitFor();
				
				int state = stopApp.waitFor();
				
				System.out.println("First application stop finished with state " + State.toString(state));
			}
			
			{
				App app = server.start("linkedjava");
				app.waitFor(State.RUNNING);
				App stopApp = server.connect("stopjava");
				
				app.kill();
				app.waitFor();
				
				int state = stopApp.waitFor();
				
				System.out.println("Second application stop finished with state " + State.toString(state));
			}
		}
		
		server.terminate();
		This.terminate();
	}

}