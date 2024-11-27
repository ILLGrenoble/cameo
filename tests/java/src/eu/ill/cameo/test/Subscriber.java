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
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.common.messages.Messages;


public class Subscriber {

	public static void main(String[] args) {
		
		This.init(args);
		
		String applicationName = null;
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("Publisher application is " + applicationName);
		}
		else {
			System.err.println("Arguments: [application name]");
			System.exit(-1);
		}
		
		Server server = This.getServer();
		
		try {
			App publisherApplication = server.connect(applicationName);
			
			System.out.println("Subscribing publisher...");
			eu.ill.cameo.api.coms.Subscriber subscriber = eu.ill.cameo.api.coms.Subscriber.create(publisherApplication, "publisher");
			subscriber.init();
			System.out.println("Synchronized with 1 publisher");
			
			// Receive data.
			while (true) {
				byte[][] data = subscriber.receiveTwoParts();
				if (data != null) {
					System.out.println("Received " + Messages.parseString(data[0]) + ", " + Messages.parseString(data[1]));
				}
				else {
					break;
				}
			}
			
			System.out.println("Finished stream");
		}
		catch (InitException e) {
			System.out.println("Cannot create subscriber");
		}
		finally {
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}