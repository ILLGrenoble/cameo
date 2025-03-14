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
 * Class defining a waiting for the Publisher class.
 *
 */
public class PublisherWaiting extends Waiting {

	private Publisher publisher;

	/**
	 * Constructor.
	 * @param publisher The publisher.
	 */
	public PublisherWaiting(Publisher publisher) {
		this.publisher = publisher;
	}
	
	@Override
	public void cancel() {
		publisher.cancel();
	}

	@Override
	public void terminate() {
		publisher.terminate();
	}
	
}