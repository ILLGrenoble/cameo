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

import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.RemoteException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.messages.Messages;


public class BasicResponder {

	public static void main(String[] args) {

		This.init(args);
		
		try {
			System.out.println("Creating responder");
			
			// Create the responder.
			fr.ill.ics.cameo.coms.basic.Responder responder = fr.ill.ics.cameo.coms.basic.Responder.create("responder");
			
			// Set the state.
			This.setRunning();
			
			
			// Receive the first request.
			fr.ill.ics.cameo.coms.basic.Request request = responder.receive();
			System.out.println("Received request " + request.get());

			// Reply.
			request.reply("1st response");
			request.terminate();
			
			// Receive the second request.
			request = responder.receive();
			
			byte[][] data = request.getTwoBinaryParts();
			System.out.println("Received request " + Messages.parseString(data[0]) + " " + Messages.parseString(data[1]));

			// Reply.
			request.reply("2nd response");
			request.terminate();
			
			// Receive the third request.
			request = responder.receive();
			System.out.println("Received request " + request.get());

			// Reply.
			request.reply("3rd response");
			request.terminate();
			
			System.out.println("Replied 3rd response");			
						
			
			
			
			// Receive the fourth request.
			request = responder.receive();
			System.out.println("Received request " + request.get());

			System.out.println("Wait so that the requester has timed out");			
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}
			
			// Reply.
			request.reply("4th response");
			request.terminate();
			
			
			// Test connection.
			Instance requester = request.connectToRequester();
			System.out.println("Requester is " + requester);
						
			request.terminate();
			
			// Terminate the responder.
			responder.terminate();
			
		} catch (RemoteException e) {
			System.out.println("Responder error");
			
		} finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}