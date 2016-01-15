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


public class TestResponderApplication {

	public static void main(String[] args) {

		Application.This.init(args);
		
		if (Application.This.isAvailable()) {
			System.out.println("connected");
		}
		
		try {
			System.out.println("creating responder");
			
			// create the publisher
			Application.Responder responder = Application.Responder.create("responder");
			
			Application.This.setRunning();
			
			Application.Request request = responder.receive();
			
			System.out.println("received request " + request);
			
			request.send("done");
			
		} catch (RemoteException e) {
			System.out.println("responder error");
			
		} finally {
			// Do not forget to terminate This.
			Application.This.terminate();			
		}
		
		System.out.println("finished the application");
	}

}