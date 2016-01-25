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

import java.util.List;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.Application.Instance;
import fr.ill.ics.cameo.Option;
import fr.ill.ics.cameo.OutputPrintThread;
import fr.ill.ics.cameo.OutputStreamSocket;
import fr.ill.ics.cameo.Server;

public class Test {
	
	public static void testFast(Server server) {
		
		server.killAllAndWaitFor("fast");
		
		int i = 1;
		while (i > 0) {
					
			final Instance result = server.start("fast");
			if (result.exists()) {
				System.out.println("new application " + result.getNameId());
			} else {
				System.err.println("cannot start application");
				return;
			}
			
			// test the kill in parallel
			new Thread(new Runnable() {
			    @Override
			    public void run() {
			    	try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}			    	
			    	result.kill();
			    }
			}).start();

			result.waitFor();
			
			i++;
		}
	}

	public static void testFastAsync(Server server) {
		
		server.killAllAndWaitFor("fast");
		
		int i = 1;
		while (i > 0) {
		
			Instance result = server.start("fast");
			if (result.exists()) {
				System.out.println("new application " + result.getNameId());
			} else {
				System.err.println("cannot start application");
				return;
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			
			// replace the result
			result.kill();
			result.waitFor();
			
			i++;
		}
	}
	
	public static void testFastOut(Server server, int sleep) {
		
		server.killAllAndWaitFor("fastout");
		
		int i = 1;
		while (i > 0) {
		
			Instance result = server.start("fastout", Option.OUTPUTSTREAM);
			if (result.exists()) {
				System.out.println("new application " + result.getNameId());
			} else {
				System.err.println("cannot start application");
				return;
			}
			
			// start output thread
			OutputStreamSocket streamSocket = result.getOutputStreamSocket();

			// the socket can be null if the application is already terminated
			if (streamSocket == null) {
				System.out.println("cannot get output stream");
				continue;				
			}
			
			OutputPrintThread outputThread = new OutputPrintThread(streamSocket);
			outputThread.start();
						
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
			}
			
			result.kill();
			
			result.waitFor();
			outputThread.waitFor();
			
			i++;
		}
	}
	
	public static void testFastParallel(Server server) {
		
		server.killAllAndWaitFor("fastpar");
		
		final Server localServer = server;
		
		// start 10 threads
		for (int i = 0; i < 10; ++i) {
		
			new Thread(new Runnable()
			{
				@Override
				public void run() {

					System.out.println("started thread " + this);
					
					int i = 1;
					while (i > 0) {
					
						Instance result = localServer.start("fastpar");
						if (result.exists()) {
							System.out.println("new application " + result.getNameId());
						} else {
							System.err.println("cannot start application");
							return;
						}
						
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
						}
						
						result.kill();
						result.waitFor();
						
						i++;
					}
					
				}
				
			}).start();
		}
	}
	
	public static void testFastConnect(Server server) {
		
		server.killAllAndWaitFor("fastpar");
		
		int i = 1;
		while (i > 0) {
			
			for (int j = 0; j < 10; ++j) {
				Instance result = server.start("fastpar");
				System.out.println("new application " + result.getNameId());
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			
			// connect and kill
			List<Instance> results = server.connectAll("fastpar");
			
			for (Instance result : results) {
				result.kill();
				int state = result.waitFor();
				System.out.println("killed " + result.getNameId() 
									+ " with initial state " + Application.State.toString(result.getInitialState())
									+ " and final state " + Application.State.toString(state));
			}
			
			i++;
		}
	}
	
	public static void testError(Server server) {
		
		server.killAllAndWaitFor("error");
		
		int i = 1;
		while (i > 0) {
			
			Instance result = server.start("error");
			if (result.exists()) {
				System.out.println("new application " + result.getNameId());
			} else {
				System.err.println("cannot start application");
				return;
			}

			int state = result.waitFor();
			if (state == Application.State.SUCCESS) {
				System.out.println("error, the application should fail");
			} else {
				System.out.println("ok, the application failed");
			}
						
			i++;
		}
	}
	
	public static void testNoApp(Server server) {
		
		int i = 1;
		while (i > 0) {
		
			Instance result = server.start("noapp");
			if (result.exists()) {
				System.out.println("new application " + result.getNameId());
			} else {
				System.err.println("cannot start application");
				return;
			}

			int state = result.waitFor();
			if (state == Application.State.SUCCESS) {
				System.out.println("error, the application should fail");
			} else {
				System.out.println("ok, the application failed");
			}
						
			i++;
		}
	}
	
	public static void main(String[] args) {

		// ex: tcp://localhost:7000 fastout1000
		if (args.length < 2) {
			System.out.printf("syntax: test <endpoint> <test>\n");
			return;
		}

		Server server = new Server(args[0]);
		
		// loop with fast application
		try {
			if (!server.isAvailable()) {
				System.out.println("no connection to server");
				return;
			}
		
			// manage options
			String testArg = args[1];
						
			if (testArg.equals("fast")) {
				testFast(server);	
			} else if (testArg.equals("fastasync")) {
				testFastAsync(server);	
			} else if (testArg.equals("fastout10")) {
				testFastOut(server, 10);
			} else if (testArg.equals("fastout1000")) {
				testFastOut(server, 1000);
			} else if (testArg.equals("fastparallel")) {
				testFastParallel(server);
			} else if (testArg.equals("fastconnect")) {
				testFastConnect(server);
			} else if (testArg.equals("error")) {
				testError(server);
			} else if (testArg.equals("noapp")) {
				testNoApp(server);
			} else {
				System.out.println("no test " + testArg);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			server.terminate();
		}

	}
}