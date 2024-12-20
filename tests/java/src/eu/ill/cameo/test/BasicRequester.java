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
import eu.ill.cameo.api.base.ServerAndApp;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.coms.Requester;


public class BasicRequester {

	public static void main(String[] args) {

		This.init(args);
		
		boolean useProxy = false;
		if (args.length > 1) {
			useProxy = Boolean.parseBoolean(args[0]);
		}
		
		try {
			System.out.println("Creating requester");
			
			// Get the starter app.
			ServerAndApp starter = This.connectToStarter((useProxy ? Option.USE_PROXY : 0));
			
			// Create the requester.
			Requester requester = Requester.create(starter.getApp(), "responder");
			requester.init();
			
			// Set the state.
			This.setRunning();

			// Send 10 requests.
			int R = 10;
			for (int i = 0; i < R; ++i) {
				// Send and wait for the result.
				requester.sendString("test");
				String result = requester.receiveString();
				
				System.out.println("Received " + result);
			}
			
			// Terminate the requester.
			requester.terminate();
			starter.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}