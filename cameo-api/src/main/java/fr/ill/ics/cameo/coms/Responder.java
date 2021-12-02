package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Application.This;
import fr.ill.ics.cameo.ResponderCreationException;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.coms.impl.RequestImpl;
import fr.ill.ics.cameo.coms.impl.ResponderImpl;
import fr.ill.ics.cameo.impl.ThisImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;

/**
 * Class Responder.
 *
 */
public class Responder {

	private ResponderImpl impl;
	
	private Responder(ResponderImpl impl) {
		this.impl = impl;
	}
	
	private static ResponderImpl createResponder(String name) throws ResponderCreationException {
		
		String portName = ResponderImpl.RESPONDER_PREFIX + name;
		Zmq.Msg request = ThisImpl.createRequestPortV0Request(This.getId(), portName);

		JSONObject response = This.getCom().request(request);
		
		int port = JSON.getInt(response, Message.RequestResponse.VALUE);
		if (port == -1) {
			throw new ResponderCreationException(JSON.getString(response, Message.RequestResponse.MESSAGE));
		}
		
		return new ResponderImpl(This.getCom().getImpl(), port, name);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws ResponderCreationException, ConnectionTimeout
	 */
	static public Responder create(String name) throws ResponderCreationException {
		return new Responder(createResponder(name));
	}
	
	public String getName() {
		return impl.getName();
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
		impl.terminate();
	}
		
	@Override
	public String toString() {
		return impl.toString();
	}
}
