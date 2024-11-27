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

import eu.ill.cameo.api.base.TimeoutCounter;
import eu.ill.cameo.common.strings.Endpoint;

public interface RequesterImpl {

	void setPollingTime(int value);
	void setTimeout(int value);
	
	void init(Endpoint endpoint, String responderIdentity, TimeoutCounter timeoutCounter);
	
	void send(byte[] requestData);
	void send(String request);
	void sendTwoParts(byte[] requestData1, byte[] requestData2);
	
	byte[] receive();
	String receiveString();
	
	void cancel();
	boolean isCanceled();
	
	boolean hasTimedout();
	
	void terminate();
	
}