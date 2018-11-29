/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo;

import fr.ill.ics.cameo.impl.ServerImpl;
import fr.ill.ics.cameo.impl.TimeCondition;

public class ConnectionChecker {

	public static interface Handler {
		
		void handle(boolean available);
	}
	
	private ServerImpl server;
	private Handler handler;
	private Thread thread = null;
	private TimeCondition waitCondition = new TimeCondition();
		
	public ConnectionChecker(ServerImpl server, Handler handler) {
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
	
	public synchronized void terminate() {
		if (thread != null) {
			waitCondition.notifyCondition();
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
	}
}