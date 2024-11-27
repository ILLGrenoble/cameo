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
import eu.ill.cameo.api.base.OutputStreamSocket;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.This;


public class TestStream {

	public static void main(String[] args) {

		This.init(args);
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 1) {
			useProxy = Boolean.parseBoolean(args[0]);
		}
		if (useProxy) {
			endpoint = "tcp://localhost:12000";
		}
		
		Server server = Server.create(endpoint, (useProxy ? Option.USE_PROXY : 0));
		server.init();
		
		// Start the application.
		App app = server.start("streamjava", Option.OUTPUTSTREAM);

		OutputStreamSocket socket = app.getOutputStreamSocket();
		
		// Start thread.
		Thread outputThread = new Thread(() -> {
			while (true) {
				OutputStreamSocket.Output output = socket.receive();
		    	if (output != null) {
		    		System.out.println(output.getMessage());
		    	}
		    	else {
		    		return;
		    	}
			}
		});
		
		outputThread.start();
		
		// Wait before canceling output.
		try {
			Thread.sleep(1000);
				
			System.out.println("Canceling output");
			socket.cancel();
			outputThread.join();
		}
		catch (InterruptedException e) {
		}

		app.waitFor();
		
		server.terminate();
		This.terminate();
		
		System.out.println("Finished the application");
	}

}