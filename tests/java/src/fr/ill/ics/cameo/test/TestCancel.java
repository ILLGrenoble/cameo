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
import fr.ill.ics.cameo.base.RemoteException;
import fr.ill.ics.cameo.base.This;


public class TestCancel {

	public static void main(String[] args) {

		This.init(args);
		
		try {
			// Test This.cancelWaitings.
			{
				System.out.println("Starting stopjava for cancelWaitings");
				
				Instance stopApplication = This.getServer().start("stopjava");
	
				// Start thread.
				Thread cancel = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		This.cancelWaitings();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancel.start();
				
				int state = stopApplication.waitFor();
	
				System.out.println("End of waitFor with state " + Application.State.toString(state));
	
				stopApplication.stop();
				state = stopApplication.waitFor();
	
				System.out.println("End of stopjava with state " + Application.State.toString(state));
	
				cancel.join();
			}
			
			// Test Instance.cancelWaitFor.
			{
				System.out.println("Starting stopjava for cancelWaitFor");
				
				final Instance stopApplication = This.getServer().start("stopjava");
	
				// Start thread
				Thread cancel = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		stopApplication.cancelWaitFor();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancel.start();
				
				int state = stopApplication.waitFor();
				
				System.out.println("End of waitFor with state " + Application.State.toString(state));
	
				stopApplication.stop();
				state = stopApplication.waitFor();
	
				System.out.println("End of stopjava with state " + Application.State.toString(state));
	
				cancel.join();
			}
			
			// Test Publisher.cancelWaitForSubscribers.
			{
				System.out.println("Creating publisher and waiting for 1 subscriber...");
				
				// create the publisher
				final fr.ill.ics.cameo.coms.Publisher publisher = fr.ill.ics.cameo.coms.Publisher.create("publisher", 1);
				
				Thread cancel = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		publisher.cancelWaitForSubscribers();	
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancel.start();
				
				System.out.println("Wait for subscribers");
				
				boolean synced = publisher.waitForSubscribers();
							
				cancel.join();
				
				System.out.println("Synchronization with the subscriber " + synced);
				
				publisher.terminate();
			}
			
			// Test the killing of the application.
			{
				System.out.println("Starting publisherloopjava for killing");
				
				final Instance pubLoopApplication = This.getServer().start("publisherloopjava");
	
				// Start thread
				Thread cancel = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		pubLoopApplication.kill();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancel.start();
				
				fr.ill.ics.cameo.coms.Subscriber subscriber = fr.ill.ics.cameo.coms.Subscriber.create(pubLoopApplication, "publisher");
								
				while (true) {
					String data = subscriber.receiveString();
					if (data == null) {
						System.out.println("Exiting loop");
						break;
						
					} else {
						System.out.println("Received " + data);
					}
				}
				
				System.out.println("Subscriber end of stream " + subscriber.isEnded());

				int state = pubLoopApplication.waitFor();

				System.out.println("End of waitFor " + Application.State.toString(state));
	
				cancel.join();
				
				subscriber.terminate();
			}
			
			// Test the cancelling of a subscriber.
			{
				System.out.println("Starting publisherloopjava for testing cancel of a subscriber");
				
				final Instance pubLoopApplication = This.getServer().start("publisherloopjava");
				
				fr.ill.ics.cameo.coms.Subscriber subscriber = fr.ill.ics.cameo.coms.Subscriber.create(pubLoopApplication, "publisher");

				// Start thread
				Thread cancel = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		This.cancelWaitings();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancel.start();
				
				
				while (true) {
					String data = subscriber.receiveString();
					if (data == null) {
						System.out.println("Exiting loop");
						break;
						
					} else {
						System.out.println("Received " + data);
					}
				}
				
				System.out.println("Subscriber end of stream " + subscriber.isEnded());

				// Start thread
				Thread kill = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		pubLoopApplication.kill();
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				kill.start();
				
				int state = pubLoopApplication.waitFor();

				System.out.println("End of waitFor " + Application.State.toString(state));
	
				cancel.join();
				kill.join();
				
				subscriber.terminate();
			}
			
			// Test the Responder.
			{
				System.out.println("Creating responder and waiting for requests");
				
				// Create the publisher.
				final fr.ill.ics.cameo.coms.legacy.Responder responder = fr.ill.ics.cameo.coms.legacy.Responder.create("responder");
				
				Thread cancel = new Thread(new Runnable() {
					@Override
				    public void run() {
				    	try {
				    		Thread.sleep(1000);
				    		responder.cancel();	
				    		
				    	} catch (InterruptedException e) {
						}
				    }
				});
				
				cancel.start();
				
				System.out.println("Wait for requests");
				
				fr.ill.ics.cameo.coms.legacy.Request request = responder.receive();
				
				if (request != null) {
					System.err.println("Responder error: receive should return null");		
				}
				
				cancel.join();
			}
			
		} catch (RemoteException e) {
			System.err.println("Publisher error");
			
		} catch (InterruptedException e) {

		} finally {
			This.terminate();			
		}
		
		System.out.println("Finished the application");
	}

}