/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.base;

/**
 * Class providing a simple connection checker.
 */
public class ConnectionChecker {

	public static interface Handler {
		
		void handle(boolean available);
	}
	
	private Server server;
	private Handler handler;
	private Thread thread = null;
	private TimeCondition waitCondition = new TimeCondition();
	
	ConnectionChecker(Server server, Handler handler) {
		this.server = server;
		this.handler = handler;
	}

	void start(final int timeoutMs, final int pollingTimeMs) {

		thread = new Thread(new Runnable() {
			public void run() {
	
				while (true) {
					
					boolean stopped = waitCondition.waitFor(pollingTimeMs);
					if (stopped) {
						return;
					}

					// Check the server.
					boolean available = (server.isAvailable(timeoutMs));
					
					// Apply the handler.
					handler.handle(available);
				}
			}
		});
		
		thread.start();
	}

	/**
	 * Terminates the checker.
	 */
	public synchronized void terminate() {
		if (thread != null) {
			waitCondition.notifyCondition();
			try {
				thread.join();
			}
			catch (InterruptedException e) {
			}
		}
	}
}