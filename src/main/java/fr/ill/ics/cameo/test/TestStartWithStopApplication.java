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


public class TestStartWithStopApplication {

	public static void main(String[] args) {

		Application.This.init(args);
		Server server = Application.This.getServer();
		
		if (Application.This.isAvailable() && server.isAvailable()) {
			System.out.println("application connected");
		} else {
			System.exit(-1);
		}
		
		try {
			Application.Instance stopApplication = server.start("stopjava");

			System.out.println("waiting 1s...");
			// stop the application after 1s
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			
			System.out.println("stopping application " + stopApplication.getNameId());
			stopApplication.stop();
			
			// the getString is blocking until the application finishes
			String result = stopApplication.getStringResult();
						
			System.out.println("stop application returned " + result);
						
		} finally {
			// do not forget to terminate the server and application
			Application.This.terminate();
		}
		
		System.out.println("finished the application");
	}

}