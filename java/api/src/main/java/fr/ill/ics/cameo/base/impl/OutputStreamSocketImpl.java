package fr.ill.ics.cameo.base.impl;

import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.Context;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.messages.JSON.Parser;
import fr.ill.ics.cameo.strings.Endpoint;

public interface OutputStreamSocketImpl {

	void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser);
	void setApplicationId(int id);
	Application.Output receive();
	boolean isEnded();
	boolean isCanceled();
	void cancel();
	void terminate();
}
