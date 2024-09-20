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
