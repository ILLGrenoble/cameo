/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms;

import eu.ill.cameo.api.base.Waiting;

/**
 * Class defining a waiting for the Requester class.
 */
public class RequesterWaiting extends Waiting {

	private Requester requester;

	/**
	 * Constructor.
	 * @param requester The requester.
	 */
	public RequesterWaiting(Requester requester) {
		this.requester = requester;
	}
	
	@Override
	public void cancel() {
		requester.cancel();
	}

	@Override
	public void terminate() {
		requester.terminate();
	}
	
}