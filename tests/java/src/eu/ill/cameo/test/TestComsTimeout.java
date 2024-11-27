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
import eu.ill.cameo.api.coms.Publisher;


public class TestComsTimeout {

	public static void main(String[] args) {
		
		This.init(args);
		
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
		
		// Set the state.
		This.setRunning();
		
		try {
			App app = server.start("comstimeoutjava");
			
			app.waitFor(State.RUNNING);

			Publisher publisher = Publisher.create("pub");
			publisher.setWaitForSubscribers(2);
			
			Thread initThread = new Thread(() -> {
				publisher.init();
			});
			initThread.start();
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			
			publisher.cancel();
			try {
				initThread.join();
			} catch (InterruptedException e) {
			}
			
			app.waitFor();
		}
		finally {
			server.terminate();
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}