package eu.ill.cameo.api.base.impl.zmq;

import eu.ill.cameo.api.base.Context;
import eu.ill.cameo.com.Zmq;

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
