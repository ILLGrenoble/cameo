package fr.ill.ics.cameo.base.impl;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.Context;

public class ContextImpl implements Context {
	
	private Zmq.Context context;
	
	ContextImpl(Zmq.Context context) {
		this.context = context;
	}
	
	public Zmq.Context getContext() {
		return context;
	}
}
