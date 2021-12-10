package fr.ill.ics.cameo.base.impl;

import fr.ill.ics.cameo.base.Event;

public interface EventStreamSocketImpl {

	void init();
	Event receive();
	boolean isCanceled();
	void cancel();
	void destroy();
}
