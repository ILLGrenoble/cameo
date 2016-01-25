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
import fr.ill.ics.cameo.RemoteException;
import fr.ill.ics.cameo.Server;


public class TestRequesterAndResponderApplication {

	public static void main(String[] args) {
		
		Application.This.init(args);
		
		String applicationName = null;
		if (args.length > 1) {
			applicationName = args[0];
			System.out.println("responder application is " + applicationName);
			
		} else {
			System.err.println("arguments: [application name]");
			System.exit(-1);
		}
		
		// get the client services
		Server server = Application.This.getServer();
		
		if (Application.This.isAvailable() && server.isAvailable()) {
			System.out.println("connected application");
			System.out.println("connected server " + server);
		} else {
			System.exit(-1);
		}
		
		try {
			Application.Instance responderApplication = server.start(applicationName);
			
			System.out.println("started application " + responderApplication);

			Application.Requester requester = Application.Requester.create(responderApplication, "responder");
			
			System.out.println("created requester " + requester);
			
			requester.send("request");
			
			System.out.println("response is " + requester.receiveString());
			
			int state = responderApplication.waitFor();
			System.out.println("responder application terminated with state " + Application.State.toString(state));
			
		} catch (RemoteException e) {
			System.out.println("requester error:" + e);
			
		} finally {
			// Do not forget to terminate This.
			Application.This.terminate();
		}
		
		System.out.println("finished the application");
	}

}