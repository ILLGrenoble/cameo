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


public class Stream {
	
	public static void main(String[] args) {

		This.init(args);
					
		int i = 0;
		while (i < 20) {
			System.out.println("Printing " + i);
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
			}
			i++;
		}
		
		System.out.println("Finished the application");
		
		This.terminate();			
	}

}