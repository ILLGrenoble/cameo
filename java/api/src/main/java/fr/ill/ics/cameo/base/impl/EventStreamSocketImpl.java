package fr.ill.ics.cameo.base.impl;

import fr.ill.ics.cameo.base.Context;
import fr.ill.ics.cameo.base.Event;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.messages.JSON.Parser;
import fr.ill.ics.cameo.strings.Endpoint;

public interface EventStreamSocketImpl {

	void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser);
	Event receive();
	boolean isCanceled();
	void cancel();
	void terminate();
}
