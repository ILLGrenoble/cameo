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

import eu.ill.cameo.api.base.App.Com.KeyValueGetter;

/**
 * Class defining a waiting for the KeyValueGetter class.
 *
 */
public class KeyValueGetterWaiting extends Waiting {

	private KeyValueGetter getter;

	/**
	 * Constructor.
	 * @param getter The getter.
	 */
	public KeyValueGetterWaiting(KeyValueGetter getter) {
		this.getter = getter;
	}
	
	@Override
	public void cancel() {
		getter.cancel();
	}

	@Override
	public void terminate() {
	}
	
}