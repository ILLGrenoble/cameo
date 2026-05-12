/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.test.attic;

import eu.ill.cameo.api.base.Server;

public class TestBadEndpoint {
			
	public static void main(String[] args) {
		
		try {
			Server server = Server.create("tcp://abcd.ill.fr:7000");
			server.init();
		}
		catch (Exception e) {
			System.out.println("The server has bad endpoint");
			e.printStackTrace();
		}

		try {
			Server server = Server.create("tcp://localhost:9999");
			server.setTimeout(1000);
			server.init();
		}
		catch (Exception e) {
			System.out.println("The server has bad endpoint");
			e.printStackTrace();
		}
	}
}