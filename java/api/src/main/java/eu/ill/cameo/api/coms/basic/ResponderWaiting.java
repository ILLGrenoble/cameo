/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms.basic;

import eu.ill.cameo.api.base.Waiting;

/**
 * Class defining a waiting for the Responder class.
 */
public class ResponderWaiting extends Waiting {

	private Responder responder;

	/**
	 * Constructor.
	 * @param responder The responder.
	 */
	public ResponderWaiting(Responder responder) {
		this.responder = responder;
	}
	
	@Override
	public void cancel() {
		responder.cancel();
	}

	@Override
	public void terminate() {
		responder.terminate();
	}
	
}