package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.impl.RequestImpl;
import fr.ill.ics.cameo.coms.impl.ResponderImpl;
import fr.ill.ics.cameo.coms.impl.zmq.ResponderZmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;

/**
 * Class Responder.
 *
 */
public class Responder {
	
	private String name;
	private ResponderImpl impl;
	private ResponderWaiting waiting = new ResponderWaiting(this);
	
	private Responder(String name) {
		this.name = name;
		//TODO Replace with factory.
		this.impl = new ResponderZmq();
		waiting.add();
	}
	
	private void init(String name) throws ResponderCreationException {
		
		String portName = ResponderImpl.RESPONDER_PREFIX + name;
		JSONObject request = Messages.createRequestPortV0Request(This.getId(), portName);

		JSONObject response = This.getCom().requestJSON(request);
		
		int port = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (port == -1) {
			throw new ResponderCreationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		
		impl.init(port, name);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws ResponderCreationException, ConnectionTimeout
	 */
	static public Responder create(String name) throws ResponderCreationException {
		
		Responder responder = new Responder(name);
		responder.init(name);
		
		return responder;
	}
	
	public String getName() {
		return name;
	}
	
	public Request receive() {
		RequestImpl requestImpl = impl.receive();
		if (requestImpl == null) {
			return null;
		}
		return new Request(requestImpl);
	}

	public void cancel() {
		impl.cancel();			
	}
	
	public boolean isEnded() {
		return impl.isEnded();
	}
	
	public boolean isCanceled() {
		return impl.isCanceled();
	}
			
	public void terminate() {
		waiting.remove();
		impl.terminate();
	}

	@Override
	public String toString() {
		return ResponderImpl.RESPONDER_PREFIX + name + ":" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
}
