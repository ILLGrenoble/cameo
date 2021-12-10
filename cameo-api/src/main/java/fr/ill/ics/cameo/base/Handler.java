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

package fr.ill.ics.cameo.base;

import fr.ill.ics.cameo.base.Application.Handle;
import fr.ill.ics.cameo.base.Application.State;
import fr.ill.ics.cameo.base.impl.ThisImpl;

public class Handler {

	private ThisImpl application;
	private Handle handler;
	private Thread thread = null;
		
	public Handler(ThisImpl application, Handle handler) {
		this.application = application;
		this.handler = handler;
	}

	public void start() {

		thread = new Thread(new Runnable() {
			public void run() {
				int state = application.waitForStop();
				
				// Only run in case of STOPPING
				if (state == State.STOPPING) {
					handler.handle();
				}	
			}
		});
		
		thread.start();
	}
	
	public synchronized void terminate() {
		
		if (thread != null) {
			
			// Terminate the wait for stop, otherwise it will block.
			application.terminateWaitForStop();
			
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
	}
	
}