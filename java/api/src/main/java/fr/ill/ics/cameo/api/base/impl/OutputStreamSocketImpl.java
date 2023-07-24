package fr.ill.ics.cameo.api.base.impl;

import fr.ill.ics.cameo.api.base.Context;
import fr.ill.ics.cameo.api.base.OutputStreamSocket;
import fr.ill.ics.cameo.api.base.RequestSocket;
import fr.ill.ics.cameo.common.messages.JSON.Parser;
import fr.ill.ics.cameo.common.strings.Endpoint;

public interface OutputStreamSocketImpl {

	void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser);
	void setApplicationId(int id);
	OutputStreamSocket.Output receive();
	boolean hasEnded();
	boolean isCanceled();
	void cancel();
	void terminate();
}
