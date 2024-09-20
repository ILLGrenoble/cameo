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

import eu.ill.cameo.api.base.This;


public class ComPort {

	public static void main(String[] args) {
		
		This.init(args);
		
		int port1 = This.getCom().requestPort();
		System.out.println("Received port1 " + port1);
		
		This.getCom().setPortUnavailable(port1);
		System.out.println("Set port " + port1 + " unavailable");
		
		int port2 = This.getCom().requestPort();
		System.out.println("Received port2 " + port2);
		
		This.getCom().releasePort(port2);
		System.out.println("Released port2 " + port2);
		
		port2 = This.getCom().requestPort();
		System.out.println("Received port2 " + port2);

		System.out.println("Finished the application");
		
		This.terminate();
	}

}