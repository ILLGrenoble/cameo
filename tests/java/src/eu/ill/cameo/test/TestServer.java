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

import java.util.List;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.StartException;
import eu.ill.cameo.api.base.State;

public class TestServer {

	public static void main(String[] args) {

		System.out.println("Create server");
		
		Server server = Server.create("tcp://localhost:11000", 0);
		server.setTimeout(100);
		server.init();
		
		System.out.println("Testing connection");
		
		if (server.isAvailable()) {
			System.out.println("Server available");
		}
		
		System.out.println("Configs");
		
		List<App.Config> configs = server.getApplicationConfigs();
		
		for (App.Config c : configs) {
			System.out.println("  " + c.toString());
		}
		
		try {
			App instance = server.start("simplejava");
		
			int state = instance.waitFor();
		
			System.out.println("Terminated simple with state " + State.toString(state));
		}
		catch (StartException e) {
			System.out.println("App does not exist");
		}
		
		server.terminate();
	}

}