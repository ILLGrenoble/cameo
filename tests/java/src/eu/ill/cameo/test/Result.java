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


public class Result {

	public static void main(String[] args) {

		This.init(args);
		
		This.setStringResult("this is a test result");
		This.terminate();
		
		System.out.println("Finished the application");
	}

}