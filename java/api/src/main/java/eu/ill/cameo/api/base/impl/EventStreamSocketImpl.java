package fr.ill.ics.cameo.api.base.impl;

import fr.ill.ics.cameo.api.base.Context;
import fr.ill.ics.cameo.api.base.Event;
import fr.ill.ics.cameo.api.base.RequestSocket;
import fr.ill.ics.cameo.common.messages.JSON.Parser;
import fr.ill.ics.cameo.common.strings.Endpoint;

public interface EventStreamSocketImpl {

	void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser);
	Event receive();
	boolean isCanceled();
	void cancel();
	void terminate();
}
