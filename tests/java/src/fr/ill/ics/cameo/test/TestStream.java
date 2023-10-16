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

import fr.ill.ics.cameo.api.base.App;
import fr.ill.ics.cameo.api.base.Option;
import fr.ill.ics.cameo.api.base.OutputStreamSocket;
import fr.ill.ics.cameo.api.base.Server;
import fr.ill.ics.cameo.api.base.This;


public class TestStream {

	public static void main(String[] args) {

		This.init(args);
		
		boolean useProxy = false;
		String endpoint = "tcp://localhost:11000";
		if (args.length > 1) {
			useProxy = Boolean.parseBoolean(args[0]);
		}
		if (useProxy) {
			endpoint = "tcp://localhost:10000";
		}
		
		Server server = Server.create(endpoint, (useProxy ? Option.USE_PROXY : 0));
		server.init();
		
		// Start the application.
		App app = server.start("streamjava", Option.OUTPUTSTREAM);

		OutputStreamSocket socket = app.getOutputStreamSocket();
		
		// Start thread.
		Thread outputThread = new Thread(() -> {
			while (true) {
				OutputStreamSocket.Output output = socket.receive();
		    	if (output != null) {
		    		System.out.println(output.getMessage());
		    	}
		    	else {
		    		return;
		    	}
			}
		});
		
		outputThread.start();
		
		// Wait before canceling output.
		try {
			Thread.sleep(1000);
				
			System.out.println("Canceling output");
			socket.cancel();
			outputThread.join();
		}
		catch (InterruptedException e) {
		}

		app.waitFor();
		
		server.terminate();
		This.terminate();
		
		System.out.println("Finished the application");
	}

}