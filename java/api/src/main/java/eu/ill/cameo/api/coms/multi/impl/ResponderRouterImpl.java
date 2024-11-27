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

public interface ResponderRouterImpl {

	void init(String responderIdentity, String dealerEndpoint);
	void setPollingTime(int value);
	int getResponderPort();
	void cancel();
	boolean isCanceled();
	void run();
	void terminate();

}