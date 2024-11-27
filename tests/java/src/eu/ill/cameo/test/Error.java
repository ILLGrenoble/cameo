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

public class Error {

	public static void main(String[] args) {
		
		try {
			Thread.sleep(10);
		}
		catch (InterruptedException e) {
		}
		
		System.err.println("Error");
		
		System.exit(123);
	}

}