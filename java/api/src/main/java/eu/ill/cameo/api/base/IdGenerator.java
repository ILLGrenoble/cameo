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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class managing the ids of communication objects.
 */
public class IdGenerator {

	private static AtomicInteger id = new AtomicInteger(0);

	/**
	 * Generates a new id.
	 * @return An id.
	 */
	public static int newId() {
		return id.incrementAndGet();
	}
	
	/**
	 * Generates a new string id of the form "cameo.<id>". For example "cameo.15".
	 * @return A string id.
	 */
	public static String newStringId() {
		return "cameo." + newId();
	}
}