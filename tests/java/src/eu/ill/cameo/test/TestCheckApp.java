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

package eu.ill.cameo.test;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.State;
import eu.ill.cameo.api.base.This;


public class TestCheckApp {

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
		
		try {
			// loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {

				int N = 100;
				
				App[] apps = new App[N];
				
				int counter = 0;
				boolean[] appFinished = new boolean[N];
				
				for (int j = 0; j < N; ++j) {
					apps[j] = server.start("veryfastjava");
					appFinished[j] = false;
				}
	
				while (counter < N) {

					for (int j = 0; j < N; ++j) {
						if (!appFinished[j] && apps[j].getLastState() == State.SUCCESS) {
							counter++;
							appFinished[j] = true;
							System.out.println("App " + j + " finished");
						}
					}
				}

				System.out.println("Finished loop\n");
			}
		}
		finally {
			// Do not forget to terminate the server and application.
			server.terminate();
			This.terminate();
		}
	}

}