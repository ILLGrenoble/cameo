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
import fr.ill.ics.cameo.Server;

public class TestTimeoutApplication {
			
	public static void main(String[] args) {

		Application.This.init(args);
		
		Server server = null;
		
		// loop with fast application
		try {
			server = new Server("tcp://localhost:9000");
			
			while (server.isAvailable(1000)) {
				
				System.out.println("server is available");
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			
			System.out.println("server is not available");
									
		} finally {
			if (server != null) {
				server.terminate();
			}	
		}

		Application.This.terminate();
	}
}