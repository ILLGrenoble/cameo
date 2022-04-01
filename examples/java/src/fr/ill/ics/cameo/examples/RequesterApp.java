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

package fr.ill.ics.cameo.examples;

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.RemoteException;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.State;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.Requester;


public class RequesterApp {

	public static void main(String[] args) {
		
		This.init(args);
		
		String requestMessage = "";
		int N = 1;
		Server server = null;
		
		// The request message is the first argument.
		if (args.length > 1) {
			requestMessage = args[0];
		}
		
		// The number of requests is the second argument.
		if (args.length > 2) {
			N = Integer.parseInt(args[1]);
		}
		
		// The server endpoint is the third argument.
		if (args.length > 3) {
			server = new Server(args[2]);
		}
		else {	
			server = new Server(This.getEndpoint());
		}
		
		if (This.isAvailable() && server.isAvailable()) {
			System.out.println("Connected server " + server);
		} else {
			System.exit(-1);
		}
		
		try {
			// Connect to the server.
			App responderServer = server.connect("responder");
			System.out.println("Application " + responderServer + " has state " + State.toString(responderServer.getActualState()));
			
			// Create a requester.
			Requester requester = Requester.create(responderServer, "the-responder");
			System.out.println("Created requester " + requester);
			
			for (int i = 0; i < N; ++i) {
				// Send a simple message as string.
				requester.sendString(requestMessage + "-" + i);
				System.out.println("Response is " + requester.receiveString());
			}
				
			// Terminate the requester and server.
			requester.terminate();
			server.terminate();
			
		} catch (RemoteException e) {
			System.out.println("Requester error:" + e);
			
		} finally {
			// Do not forget to terminate This.
			This.terminate();
		}
		
		System.out.println("Finished the application");
	}

}