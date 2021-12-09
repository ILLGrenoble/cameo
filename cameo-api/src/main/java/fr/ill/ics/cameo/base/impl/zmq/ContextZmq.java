package fr.ill.ics.cameo.base.impl.zmq;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.Context;

public class ContextZmq implements Context {
	
	private Zmq.Context context;
	
	public ContextZmq(Zmq.Context context) {
		this.context = context;
	}
	
	public Zmq.Context getContext() {
		return context;
	}
}
