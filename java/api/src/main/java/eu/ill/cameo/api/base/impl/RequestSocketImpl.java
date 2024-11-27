/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.base.impl;

public interface RequestSocketImpl {

	void setTimeout(int timeout);
	
	byte[][] request(byte[] part1, int overrideTimeout);
	byte[][] request(byte[] part1, byte[] part2, int overrideTimeout);
	byte[][] request(byte[] part1, byte[] part2, byte[] part3, int overrideTimeout);
	
	void terminate();
}