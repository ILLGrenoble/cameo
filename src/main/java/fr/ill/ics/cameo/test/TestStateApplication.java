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


public class TestStateApplication {

	public static void main(String[] args) {

		Application.This.init(args);
		
		if (Application.This.isAvailable()) {
			System.out.println("connected");
			
		} else {
			System.exit(-1);
		}
		
		try {
			try {
				Application.This.setRunning();
				
			} catch (RemoteException e) {	
				System.out.println("error while setting the state: " + e.getMessage());
			}
			
			int i = 0;
			while (i < 5) {
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				
				System.out.println("step " + i);
				i++;
			}
			
		} finally {
			// do not forget to terminate the services
			Application.This.terminate();			
		}
		
		System.out.println("finished the application");
	}

}