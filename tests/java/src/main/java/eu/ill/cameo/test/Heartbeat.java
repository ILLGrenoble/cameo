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

import eu.ill.cameo.api.base.This;


public class Heartbeat {

	public static void main(String[] args) {

		This.init(args);
		
		boolean useProxy = false;
		if (args.length > 1) {
			useProxy = Boolean.parseBoolean(args[0]);
		}
		
		try {
			System.out.println("Creating responder");
			
			// Create the responder.
			eu.ill.cameo.api.coms.basic.Responder responder = eu.ill.cameo.api.coms.basic.Responder.create("responder");
			responder.init();
			
			System.out.println("Created responder " + responder);
			
			// Set the state.
			This.setRunning();
			
			Thread cancelThread = new Thread(new Runnable() {
				@Override
			    public void run() {
			    	try {
			    		Thread.sleep(1000);
			    		responder.cancel();	
			    	}
			    	catch (InterruptedException e) {
					}
			    }
			});
			cancelThread.start();

			responder.receive();
						
			try {
				cancelThread.join();
			}
			catch (InterruptedException e) {
			}
						
			// Terminate the responder.
			responder.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}