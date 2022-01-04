package fr.ill.ics.cameo.base.impl.zmq;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.Context;

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
