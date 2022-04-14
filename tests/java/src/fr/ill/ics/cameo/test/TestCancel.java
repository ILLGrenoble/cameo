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

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.RemoteException;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.State;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.Requester;
import fr.ill.ics.cameo.coms.basic.Request;


public class TestCancel {

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
		
		Server server = Server.create(endpoint, 0, useProxy);
		server.init();
		
		try {
			// Test This.cancelAll.
			{
				System.out.println("Starting stopjava for cancelAll");
				
				App stopApplication = server.start("stopjava");
	
				// Start thread.
				Thread cancelThread = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		This.cancelAll();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancelThread.start();
				
				int state = stopApplication.waitFor();
	
				System.out.println("End of waitFor with state " + State.toString(state));
	
				stopApplication.stop();
				state = stopApplication.waitFor();
	
				System.out.println("End of stopjava with state " + State.toString(state));
	
				cancelThread.join();
			}
			
			// Test Instance.cancelWaitFor.
			{
				System.out.println("Starting stopjava for cancelWaitFor");
				
				final App stopApplication = server.start("stopjava");
	
				// Start thread
				Thread cancelThread = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		stopApplication.cancelWaitFor();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancelThread.start();
				
				int state = stopApplication.waitFor();
				
				System.out.println("End of waitFor with state " + State.toString(state));
	
				stopApplication.stop();
				state = stopApplication.waitFor();
	
				System.out.println("End of stopjava with state " + State.toString(state));
	
				cancelThread.join();
			}
			
			// Test Publisher.cancelWaitForSubscribers.
			{
				System.out.println("Creating publisher and waiting for 1 subscriber...");
				
				// create the publisher
				final fr.ill.ics.cameo.coms.Publisher publisher = fr.ill.ics.cameo.coms.Publisher.create("publisher", 1);
				
				Thread cancelThread = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		publisher.cancelWaitForSubscribers();	
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancelThread.start();
				
				System.out.println("Wait for subscribers");
				
				boolean synced = publisher.waitForSubscribers();
							
				cancelThread.join();
				
				System.out.println("Synchronization with the subscriber " + synced);
				
				publisher.terminate();
			}
			
			// Test the killing of the application.
			{
				System.out.println("Starting publisherloopjava for killing");
				
				final App pubLoopApplication = server.start("publisherloopjava");
	
				// Start thread
				Thread cancelThread = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		pubLoopApplication.kill();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancelThread.start();
				
				fr.ill.ics.cameo.coms.Subscriber subscriber = fr.ill.ics.cameo.coms.Subscriber.create(pubLoopApplication, "publisher");
				subscriber.init();
				
				while (true) {
					String data = subscriber.receiveString();
					if (data == null) {
						System.out.println("Exiting loop");
						break;
						
					} else {
						System.out.println("Received " + data);
					}
				}
				
				System.out.println("Subscriber end of stream " + subscriber.hasEnded());

				int state = pubLoopApplication.waitFor();

				System.out.println("End of waitFor " + State.toString(state));
	
				cancelThread.join();
				
				subscriber.terminate();
			}
			
			// Test the cancelling of a subscriber.
			{
				System.out.println("Starting publisherloopjava for testing cancel of a subscriber");
				
				final App pubLoopApplication = server.start("publisherloopjava");
				
				fr.ill.ics.cameo.coms.Subscriber subscriber = fr.ill.ics.cameo.coms.Subscriber.create(pubLoopApplication, "publisher");
				subscriber.init();
				
				// Start thread.
				Thread cancelThread = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		This.cancelAll();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancelThread.start();
				
				
				while (true) {
					String data = subscriber.receiveString();
					if (data == null) {
						System.out.println("Exiting loop");
						break;
						
					} else {
						System.out.println("Received " + data);
					}
				}
				
				System.out.println("Subscriber end of stream " + subscriber.hasEnded());

				// Start thread.
				Thread killThread = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		pubLoopApplication.kill();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				killThread.start();
				
				int state = pubLoopApplication.waitFor();

				System.out.println("End of waitFor " + State.toString(state));
	
				cancelThread.join();
				killThread.join();
				
				subscriber.terminate();
			}
			
			// Test the basic Responder.
			{
				System.out.println("Creating basic responder and waiting for requests");
				
				// Create the responder.
				final fr.ill.ics.cameo.coms.basic.Responder responder = fr.ill.ics.cameo.coms.basic.Responder.create("responder");
				responder.init();
				
				// Start thread.
				Thread cancelThread = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		responder.cancel();	
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancelThread.start();
				
				System.out.println("Wait for requests");
				
				fr.ill.ics.cameo.coms.basic.Request request = responder.receive();
				
				if (request != null) {
					System.err.println("Responder error: receive should return null");		
				}
				
				cancelThread.join();
				
				responder.terminate();
			}
			
			// Test the basic Requester.
			{
				System.out.println("Creating basic responder and requester");
				
				// Create the responder.
				final fr.ill.ics.cameo.coms.basic.Responder responder = fr.ill.ics.cameo.coms.basic.Responder.create("responder");
				responder.init();

				// Start thread.
				Thread responderThread = new Thread(new Runnable() {
					@Override
				    public void run() {
						while (true) {
							Request request = responder.receive();
							if (request == null) {
								break;
							}
						}
				    }
				});
				
				responderThread.start();

				
				// Get this app.
				final App thisApp = server.connect(This.getName());
				
				// Create the requester.
				final Requester requester = Requester.create(thisApp, "responder");
				requester.init();
				
				// Start thread.
				Thread cancelThread = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		requester.cancel();
				    		responder.cancel();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancelThread.start();
				
				System.out.println("Sending request");
				
				requester.sendString("request");
				
				System.out.println("Receiving response");
				
				requester.receive();
				
				if (requester.isCanceled()) {
					System.out.println("Requester is canceled");	
				}
				else {
					System.out.println("Requester is not canceled");	
				}
				
				cancelThread.join();
				responderThread.join();
				
				responder.terminate();
				requester.terminate();
			}
			
			// Test the multi responder.
			{
				// Create the responder.
				final fr.ill.ics.cameo.coms.multi.ResponderRouter router = fr.ill.ics.cameo.coms.multi.ResponderRouter.create("responder");
				router.init();
				
				final fr.ill.ics.cameo.coms.multi.Responder responder = fr.ill.ics.cameo.coms.multi.Responder.create(router);
				responder.init();

				Thread routerThread = new Thread(new Runnable() {
					@Override
				    public void run() {
						router.run();
				    }
				});
				
				routerThread.start();
				
				Thread responderThread = new Thread(new Runnable() {
					@Override
				    public void run() {
						responder.receive();
				    }
				});
				
				responderThread.start();
				
				responder.cancel();
				router.cancel();
				
				if (responder.isCanceled() && router.isCanceled()) {
					System.out.println("Router and responder are canceled");
				}
				else {
					System.out.println("Router and responder are not canceled");
				}
				
				responderThread.join();
				routerThread.join();
			}
		}
		catch (RemoteException e) {
			System.err.println("Error: " + e.getMessage());
		}
		catch (InterruptedException e) {
		}
		finally {
			server.terminate();
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}