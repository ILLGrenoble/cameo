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

package fr.ill.ics.cameo.test;

import fr.ill.ics.cameo.api.base.App;
import fr.ill.ics.cameo.api.base.Option;
import fr.ill.ics.cameo.api.base.Server;
import fr.ill.ics.cameo.api.base.State;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.coms.Publisher;


public class TestComsTimeout {

	public static void main(String[] args) {
		
		This.init(args);
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 3) {
			useProxy = Boolean.parseBoolean(args[2]);
		}
		if (useProxy) {
			endpoint = "tcp://localhost:10000";
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