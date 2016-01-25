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


public class TestResultApplication {

	public static void main(String[] args) {

		Application.This.init(args);
		
		if (Application.This.isAvailable()) {
			System.out.println("connected");
			
		} else {
			System.exit(-1);
		}

		System.out.println("waiting 1s...");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
		Application.This.setResult("test result");
		Application.This.terminate();
		
		System.out.println("finished the application");
	}

}