package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Application.This;
import fr.ill.ics.cameo.coms.impl.RequestImpl;
import fr.ill.ics.cameo.coms.impl.ResponderImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;

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
		JSONObject request = Messages.createRequestPortV0Request(This.getId(), portName);

		JSONObject response = This.getCom().requestJSON(request);
		
		int port = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (port == -1) {
			throw new ResponderCreationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		
		return new ResponderImpl(port, name);
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
