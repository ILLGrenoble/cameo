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
import eu.ill.cameo.api.base.KeyValue;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.This;


public class TestWaitStorage {

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
		
		String key = "eu.ill.cameo.test.testkey";
		
		try {
			// Loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {
			
				// Start the application.
				App application = server.start("waitstoragejava");
	
				// Define a KeyValue.
				KeyValue keyValue = new KeyValue(key);
				
				application.waitFor(keyValue);
				System.out.println("Storage event " + keyValue.getStatus() + " " + keyValue.getValue());
				
				// Get the key value.
				try {
					System.out.println("Key value " + application.getCom().getKeyValue(key));
				}
				catch (Exception e) {
				}
				
				application.waitFor(keyValue);
				System.out.println("Storage event " + keyValue.getStatus() + " " + keyValue.getValue());
								
				application.waitFor();
				System.out.println("Finished application " + application);
			}
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application after " + ((new Date()).getTime() - d.getTime()) + "ms");
	}

}