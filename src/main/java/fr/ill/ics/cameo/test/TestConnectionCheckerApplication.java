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

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionChecker;
import fr.ill.ics.cameo.Server;

public class TestConnectionCheckerApplication {
			
	static boolean stop = false;
	
	public static void main(String[] args) {

		Application.This.init(args);
		
		Server server = null;
		ConnectionChecker connectionChecker = null;
		
		// loop with fast application
		try {
			server = new Server("tcp://localhost:9000");
			server.setTimeout(1000);
						
			connectionChecker = server.createConnectionChecker(new ConnectionChecker.Handler() {
				
				@Override
				public void handle(boolean available) {
					
					if (available) {
						System.out.println("server is available");
					}
					else {
						System.out.println("server is not available");
						stop = true;
					}
				}
			}, 1000);
			
			while (!stop) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
												
		} finally {
			if (server != null) {
				connectionChecker.terminate();
				server.terminate();
			}	
		}

		Application.This.terminate();
	}
}