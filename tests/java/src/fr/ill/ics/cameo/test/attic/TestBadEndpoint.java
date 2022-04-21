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

package fr.ill.ics.cameo.test.attic;

import fr.ill.ics.cameo.base.Server;

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