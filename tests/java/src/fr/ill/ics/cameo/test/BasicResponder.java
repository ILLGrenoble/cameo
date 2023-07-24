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

import fr.ill.ics.cameo.api.base.ServerAndApp;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.common.messages.Messages;


public class BasicResponder {

	public static void main(String[] args) {

		This.init(args);
		
		boolean useProxy = false;
		if (args.length > 1) {
			useProxy = Boolean.parseBoolean(args[0]);
		}
		
		try {
			System.out.println("Creating responder");
			
			// Create the responder.
			fr.ill.ics.cameo.api.coms.basic.Responder responder = fr.ill.ics.cameo.api.coms.basic.Responder.create("responder");
			responder.init();
			
			// Set the state.
			This.setRunning();
			
			
			// Receive the first request.
			fr.ill.ics.cameo.api.coms.basic.Request request = responder.receive();
			System.out.println("Received request " + request.get());

			// Reply.
			request.replyString("1st response");
			
			// Receive the second request.
			request = responder.receive();
			
			byte[][] data = request.getTwoParts();
			System.out.println("Received request " + Messages.parseString(data[0]) + " " + Messages.parseString(data[1]));

			// Reply.
			request.replyString("2nd response");
			
			// Receive the third request.
			request = responder.receive();
			System.out.println("Received request " + request.get());

			// Reply.
			request.replyString("3rd response");
			
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
			request.replyString("4th response");
			
			
			// Receive the fifth request.
			request = responder.receive();
			
			// Reply.
			request.replyString("5th response");
			
			// Test connection.
			ServerAndApp requester = request.connectToRequester(0, useProxy);
			System.out.println("Requester is " + requester.getApp());
			
			requester.terminate();
			
			// Terminate the responder.
			responder.terminate();
		}
		finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}