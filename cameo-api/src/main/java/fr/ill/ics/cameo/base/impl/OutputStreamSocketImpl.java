package fr.ill.ics.cameo.base.impl;

import fr.ill.ics.cameo.base.Application;

public interface OutputStreamSocketImpl {

	void init();
	void setApplicationId(int id);
	Application.Output receive();
	boolean isEnded();
	boolean isCanceled();
	void cancel();
	void destroy();
}
