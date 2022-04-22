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

package fr.ill.ics.cameo.base;

/**
 * Helper class to provide an App instance and its associated Server instance.
 */
public class ServerAndApp {

	private Server server;
	private App app;
	
	/**
	 * Constructor.
	 * @param server The server.
	 * @param app The application.
	 */
	public ServerAndApp(Server server, App app) {
		this.server = server;
		this.app = app;
	}
	
	/**
	 * Terminates the app and the server.
	 */
	public void terminate() {
		app.terminate();
		server.terminate();
	}
	
	/**
	 * Gets the server.
	 * @return The server.
	 */
	public Server getServer() {
		return server;
	}
	
	/**
	 * Gets the app.
	 * @return The app.
	 */
	public App getApp() {
		return app;
	}
	
	
}
