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
import fr.ill.ics.cameo.Application.This;
import fr.ill.ics.cameo.RemoteException;


public class TestCancelApplication {

	public static void main(String[] args) {

		Application.This.init(args);
		
		if (Application.This.isAvailable()) {
			System.out.println("connected");
		}
		
		try {
			
			// Test This.cancelWaitings
			{
				System.out.println("starting stopjava for cancelWaitings");
				
				Application.Instance stopApplication = This.getServer().start("stopjava");
	
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
				
				stopApplication.waitFor();
	
				System.out.println("end of waitFor");
	
				stopApplication.stop();
				int state = stopApplication.waitFor();
	
				System.out.println("end of stopjava with state " + Application.State.toString(state));
	
				cancel.join();
			}
			
			// Test Instance.cancelWaitFor
			{
				System.out.println("starting stopjava for cancelWaitFor");
				
				final Application.Instance stopApplication = This.getServer().start("stopjava");
	
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
				
				stopApplication.waitFor();
	
				System.out.println("end of waitFor");
	
				stopApplication.stop();
				int state = stopApplication.waitFor();
	
				System.out.println("end of stopjava with state " + Application.State.toString(state));
	
				cancel.join();
			}
			
			// Test Publisher.cancelWaitForSubscribers
			{
				System.out.println("creating publisher and waiting for 1 subscriber...");
				
				// create the publisher
				final Application.Publisher publisher = Application.Publisher.create("publisher", 1);
				
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
				
				System.out.println("wait for subscribers");
				
				boolean synced = publisher.waitForSubscribers();
							
				cancel.join();
				
				System.out.println("synchronization with the subscriber synced ? " + synced);
			}
			
			// Test the killing of the application.
			{
				System.out.println("starting publoopjava for killing");
				
				final Application.Instance pubLoopApplication = This.getServer().start("publoopjava");
	
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
				
				Application.Subscriber subscriber = Application.Subscriber.create(pubLoopApplication, "publisher");
								
				while (true) {
					String data = subscriber.receiveString();
					if (data == null) {
						System.out.println("exiting loop");
						break;
						
					} else {
						System.out.println("received " + data);
					}
				}
				
				System.out.println("subscriber end of stream " + subscriber.hasEnded());

				int state = pubLoopApplication.waitFor();

				System.out.println("end of waitFor " + Application.State.toString(state));
	
				cancel.join();
			}
			
			// Test the Responder
			{
				System.out.println("creating responder and waiting for requests");
				
				// create the publisher
				final Application.Responder responder = Application.Responder.create("responder");
				
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
				
				System.out.println("wait for requests");
				
				Application.Request request = responder.receive();
							
				cancel.join();
				
				System.out.println("responder request " + request);
			}
			
		} catch (RemoteException e) {
			System.out.println("publisher error");
			
		} catch (InterruptedException e) {

		} finally {
			// Do not forget to terminate This
			Application.This.terminate();			
		}
		
		System.out.println("finished the application");
	}

}