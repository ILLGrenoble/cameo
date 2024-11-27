/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.base;

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