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

import java.util.Date;

import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.KeyValue;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.This;


public class TestWaitStorage {

	public static void main(String[] args) {
		
		This.init(args);
		
		int numberOfTimes = 1;
		if (args.length > 1) {
			numberOfTimes = Integer.parseInt(args[0]);
		}
		
		Server server = This.getServer();
		
		Date d = new Date();
		
		String key = "fr.ill.ics.cameo.test.testkey";
		
		try {
			// Loop the number of times.
			for (int i = 0; i < numberOfTimes; ++i) {
			
				// Start the application.
				Instance application = server.start("waitstoragejava");
	
				// Define a KeyValue.
				KeyValue keyValue = new KeyValue(key);
				
				application.waitFor(keyValue);
				System.out.println("Storage event " + keyValue.getStatus() + " " + keyValue.getValue());
				
				// Get the key value.
				try {
					System.out.println("Key value " + application.getCom().getKeyValue(key));
				}
				catch (Exception e) {
				}
				
				application.waitFor(keyValue);
				System.out.println("Storage event " + keyValue.getStatus() + " " + keyValue.getValue());
								
				application.waitFor();
				System.out.println("Finished application " + application);
			}
		}
		finally {
			This.terminate();
		}
		
		System.out.println("Finished the application after " + ((new Date()).getTime() - d.getTime()) + "ms");
	}

}