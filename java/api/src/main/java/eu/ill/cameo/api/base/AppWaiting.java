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
 * Class defining a waiting for an App.
 *
 */
public class AppWaiting extends Waiting {

	private App instance;

	/**
	 * Constructor.
	 * @param app The app.
	 */
	public AppWaiting(App app) {
		this.instance = app;
	}
	
	@Override
	public void cancel() {
		instance.cancel();
	}

	@Override
	public void terminate() {
		instance.terminate();		
	}
	
}