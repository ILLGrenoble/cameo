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

import eu.ill.cameo.api.base.ConnectionTimeout;
import eu.ill.cameo.common.strings.Endpoint;

public interface SubscriberImpl {
	
	void setPollingTime(int value);
	void setTimeout(int value);
	
	void init(int appId, Endpoint endpoint, Endpoint appStatusEndpoint, String publisherIdentity, boolean checkApp) throws ConnectionTimeout;
	boolean sync(int timeout);
	boolean hasEnded();
	boolean isCanceled();
	boolean hasTimedout();
	byte[] receive();
	byte[][] receiveTwoParts();
	String receiveString();
	void cancel();
	void terminate();
}