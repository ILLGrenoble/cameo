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

import eu.ill.cameo.api.base.Context;
import eu.ill.cameo.api.base.OutputStreamSocket;
import eu.ill.cameo.api.base.RequestSocket;
import eu.ill.cameo.common.messages.JSON.Parser;
import eu.ill.cameo.common.strings.Endpoint;

public interface OutputStreamSocketImpl {

	void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser);
	void setApplicationId(int id);
	OutputStreamSocket.Output receive();
	boolean hasEnded();
	boolean isCanceled();
	void cancel();
	void terminate();
}