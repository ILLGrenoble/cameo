/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms.multi.impl;

import eu.ill.cameo.api.coms.multi.Request;

public interface ResponderImpl {

	void init(String endpoint);
	Request receive();
	void reply(byte[] part1, byte[] part2);
	void cancel();
	boolean isCanceled();
	void terminate();
	
}