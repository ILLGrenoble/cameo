package fr.ill.ics.cameo.api.base.impl.zmq;

import fr.ill.ics.cameo.api.base.Context;
import fr.ill.ics.cameo.com.Zmq;

public class ContextZmq implements Context {
	
	private Zmq.Context context;
	
	public ContextZmq() {
		context = new Zmq.Context();
	}
	
	public Zmq.Context getContext() {
		return context;
	}
	
	public void terminate() {
		context.destroy();
	}
}
