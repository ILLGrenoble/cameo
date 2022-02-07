package fr.ill.ics.cameo.coms.legacy;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.KeyAlreadyExistsException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.ResponderCreationException;
import fr.ill.ics.cameo.coms.legacy.impl.ResponderImpl;
import fr.ill.ics.cameo.factory.ImplFactory;

/**
 * Class Responder.
 *
 */
public class Responder {
	
	private String name;
	private ResponderImpl impl;
	private ResponderWaiting waiting = new ResponderWaiting(this);
	private String key;
	
	public static String KEY = "responder-be30cdc9-dab5-45c1-88ed-27255a8b2a98";
	public static String PORT = "port";
	
	private Responder(String name) {
		this.name = name;
		this.impl = ImplFactory.createLegacyResponder();
		waiting.add();
	}
	
	private void init(String name) throws ResponderCreationException {
		
		impl.init();

		// Store the responder data.
		JSONObject responderData = new JSONObject();
		responderData.put(PORT, impl.getResponderPort());
		
		key = KEY + "-" + name;
		
		try {
			This.getCom().storeKeyValue(key, responderData.toJSONString());
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
		return impl.receive();
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
