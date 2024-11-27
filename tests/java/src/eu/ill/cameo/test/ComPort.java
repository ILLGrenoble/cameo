/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
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