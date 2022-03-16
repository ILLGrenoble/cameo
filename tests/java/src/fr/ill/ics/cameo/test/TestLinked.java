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

import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.This;


public class TestLinked {

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
			endpoint = "tcp://localhost:10000";
		}
		
		Server server = new Server(endpoint, 0, useProxy);
		
		// Loop the number of times.
		for (int i = 0; i < numberOfTimes; ++i) {
			
			{
				Instance app = server.start("linkedjava");
				app.waitFor(Application.State.RUNNING);
				Instance stopApp = server.connect("stopjava");
				stopApp.waitFor(Application.State.RUNNING);
				
				app.kill();
				app.waitFor();
				
				int state = stopApp.waitFor();
				
				System.out.println("First application stop finished with state " + Application.State.toString(state));
			}
			
			{
				Instance app = server.start("linkedjava");
				app.waitFor(Application.State.RUNNING);
				Instance stopApp = server.connect("stopjava");
				
				app.kill();
				app.waitFor();
				
				int state = stopApp.waitFor();
				
				System.out.println("Second application stop finished with state " + Application.State.toString(state));
			}
		}
		
		server.terminate();
		This.terminate();
	}

}