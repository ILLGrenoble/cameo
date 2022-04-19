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

import java.util.List;

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.AppException;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.State;

public class TestServer {

	public static void main(String[] args) {

		System.out.println("Create server");
		
		Server server = Server.create("tcp://localhost:11000", 100, false);
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
		catch (AppException e) {
			System.out.println("App does not exist");
		}
		
		server.terminate();
	}

}