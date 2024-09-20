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

package eu.ill.cameo.test;

import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.ServerAndApp;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.common.messages.Messages;


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
			eu.ill.cameo.api.coms.basic.Responder responder = eu.ill.cameo.api.coms.basic.Responder.create("responder");
			responder.init();
			
			// Set the state.
			This.setRunning();
			
			
			// Receive the first request.
			eu.ill.cameo.api.coms.basic.Request request = responder.receive();
			System.out.println("Received request " + request.getString());

			// Reply.
			request.replyString("1st response");
			request.replyString("1st response (bis)");
			
			// Receive the second request.
			request = responder.receive();
			
			byte[][] data = request.getTwoParts();
			System.out.println("Received request " + Messages.parseString(data[0]) + " " + Messages.parseString(data[1]));

			// Reply.
			request.replyString("2nd response");
			
			// Receive the third request.
			request = responder.receive();
			System.out.println("Received request " + request.getString());

			// Reply.
			request.replyString("3rd response");
			
			System.out.println("Replied 3rd response");			
			
			
			// Receive the fourth request.
			request = responder.receive();
			System.out.println("Received request " + request.getString());

			System.out.println("Wait so that the requester has timed out");			
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}
			
			// Reply.
			request.replyString("4th response");
			
			System.out.println("Replied 4th");
			
			// Receive the fifth request.
			request = responder.receive();
			System.out.println("Received request " + request.getString());
			
			// Reply.
			request.replyString("5th response");
			
			System.out.println("Replied 5th");
			
			// Test connection.
			ServerAndApp requester = request.connectToRequester((useProxy ? Option.USE_PROXY : 0));
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