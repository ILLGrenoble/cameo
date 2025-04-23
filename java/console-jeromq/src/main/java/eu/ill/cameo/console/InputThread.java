/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.console;

import java.io.BufferedInputStream;
import java.io.IOException;

import eu.ill.cameo.api.base.RemoteException;
import eu.ill.cameo.api.base.Server;

public class InputThread extends Thread {

	private Server server;
	private int applicationID;
	private boolean running = true;
	private Runnable stopHandler;
	private Thread shutdownHook;
	
	public InputThread(Server server, int applicationID, Thread shutdownHook) {
		this.server = server;
		this.applicationID = applicationID;
		this.shutdownHook = shutdownHook;
	}
	
	public void setStopHandler(Runnable handler) {
		this.stopHandler = handler;
	}
	
	public void run() {
		
		try {
			BufferedInputStream bis = new BufferedInputStream(System.in);
			
			while (running) {
				byte[] buffer = new byte[4096];
				if (bis.available() > 0) {
					int readSize = bis.read(buffer, 0, bis.available());
					String input = new String(buffer, 0, readSize - 1);
					
					if ("Q".equals(input)) {
						// Remove the shutdown hook so that the application is not killed.
						if (shutdownHook != null) {
							Runtime.getRuntime().removeShutdownHook(shutdownHook);
						}
						System.exit(0);
					}
					
					// Stop handler.
					if (stopHandler != null && "S".equals(input)) {
						stopHandler.run();
						break;
					}
					
					// adding the character which is better managed
					input += "\n";
					
					if (readSize > 1) {
						try {
							server.writeToInputStream(applicationID, input);
						}
						catch (RemoteException e) {
							System.err.println("cannot write to input stream: " + e.getMessage());
						}
					}
					
				}
				else {
					try {
						Thread.sleep(50);
					}
					catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		}
		catch (IOException e) {
			// error
		}
	}
	
	public void stopAndWaitFor() {
		if (running) {
			running = false;
			try {
				this.join();
			}
			catch (InterruptedException e) {
			}
		}	
	}
}