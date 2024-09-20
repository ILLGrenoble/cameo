package eu.ill.cameo.api.base.impl;

import eu.ill.cameo.api.base.Context;
import eu.ill.cameo.api.base.Event;
import eu.ill.cameo.api.base.RequestSocket;
import eu.ill.cameo.common.messages.JSON.Parser;
import eu.ill.cameo.common.strings.Endpoint;

public interface EventStreamSocketImpl {

	void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser);
	Event receive();
	boolean isCanceled();
	void cancel();
	void terminate();
}
