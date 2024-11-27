/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms.impl;

public interface PublisherImpl {
	
	void init(String publisherIdentity);
	
	int getPublisherPort();
	
	void sendSync();
	void send(byte[] data);
	void send(String data);
	void sendTwoParts(byte[] data1, byte[] data2);
	void sendEnd();
	boolean hasEnded();
	void terminate();
}