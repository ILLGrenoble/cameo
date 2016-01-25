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


public class TestStartWithResultApplication {

	public static void main(String[] args) {

		Application.This.init(args);
		
		Server server = Application.This.getServer();
		
		if (Application.This.isAvailable() && server.isAvailable()) {
			System.out.println("connected");
		} else {
			System.exit(-1);
		}
		
		try {
			Application.Instance resultApplication = server.start("resjava");

			// the getString is blocking until the application finishes
			String result = resultApplication.getStringResult();
			System.out.println("result application returned " + result);
						
		} finally {
			// do not forget to terminate the server and application
			Application.This.terminate();
		}
		
		System.out.println("finished the application");
	}

}