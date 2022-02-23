package fr.ill.ics.cameo.coms.basic;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.KeyAlreadyExistsException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.ResponderCreationException;
import fr.ill.ics.cameo.coms.basic.impl.ResponderImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.StringId;

/**
 * Class Responder.
 *
 */
public class Responder {
	
	private String name;
	private ResponderImpl impl;
	private ResponderWaiting waiting = new ResponderWaiting(this);
	private String key;
	
	public final static String KEY = "responder-676e576d-6102-42d8-ae24-222a7000dfa0";
	public final static String PORT = "port";
	
	private Responder(String name) {
		this.name = name;
		this.impl = ImplFactory.createBasicResponder();
		
		waiting.add();
	}
	
	private void init(String name) throws ResponderCreationException {

		// Set the key.
		key = KEY + "-" + name;
		
		// Init with the responder identity.
		impl.init(StringId.from(This.getId(), key));

		// Store the responder data.
		JSONObject jsonData = new JSONObject();
		jsonData.put(PORT, impl.getResponderPort());
		
		try {
			This.getCom().storeKeyValue(key, jsonData.toJSONString());
		}
		catch (KeyAlreadyExistsException e) {
			throw new ResponderCreationException("A responder with the name \"" + name + "\" already exists");
		}
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
		
		// Receive the request.
		Request request = impl.receive();
		
		// Do not set the responder if the request is null which happens after a cancel.
		if (request != null) {
			request.setResponder(this);
		}
		
		return request;
	}
	
	void reply(JSONObject request, byte[] response) {
		impl.reply(Messages.serialize(request), response);
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
		
		try {
			This.getCom().removeKey(key);
		}
		catch (UndefinedKeyException e) {
			// No need to treat.
		}
		
		waiting.remove();
		impl.terminate();
	}

	@Override
	public String toString() {
		return "req." + name + ":" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
	
}
