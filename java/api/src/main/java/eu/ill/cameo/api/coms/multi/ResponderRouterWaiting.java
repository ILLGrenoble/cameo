/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms.multi;

import eu.ill.cameo.api.base.Waiting;

/**
 * Class defining a waiting for the ResponderRouter class.
 */
public class ResponderRouterWaiting extends Waiting {

	private ResponderRouter router;

	/**
	 * Constructor.
	 * @param responder The responder.
	 */
	public ResponderRouterWaiting(ResponderRouter responder) {
		this.router = responder;
	}
	
	@Override
	public void cancel() {
		router.cancel();
	}

	@Override
	public void terminate() {
		router.terminate();
	}
	
}