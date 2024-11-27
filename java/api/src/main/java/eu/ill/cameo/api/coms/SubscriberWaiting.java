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
 * Class defining a waiting for the Subscriber class.
 */
public class SubscriberWaiting extends Waiting {

	private Subscriber subscriber;
	
	/**
	 * Constructor.
	 * @param subscriber The subscriber.
	 */
	public SubscriberWaiting(Subscriber subscriber) {
		this.subscriber = subscriber;
	}
	
	@Override
	public void cancel() {
		subscriber.cancel();
	}

	@Override
	public void terminate() {
		subscriber.terminate();
	}
	
}