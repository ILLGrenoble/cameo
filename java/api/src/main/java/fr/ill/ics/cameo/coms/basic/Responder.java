/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.coms.basic;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.ICancelable;
import fr.ill.ics.cameo.base.InitException;
import fr.ill.ics.cameo.base.KeyAlreadyExistsException;
import fr.ill.ics.cameo.base.StateObject;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.basic.impl.ResponderImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.AppIdentity;
import fr.ill.ics.cameo.strings.ServerIdentity;
import fr.ill.ics.cameo.strings.StringId;

/**
 * Class defining a basic responder. Requests are processed sequentially.
 */
public class Responder extends StateObject implements ICancelable {
	
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

	/**
	 * Returns the responder with name.
	 * @param name The name.
	 * @return A new Responder object.
	 */
	static public Responder create(String name) throws InitException {
		return new Responder(name);
	}
	
	/**
	 * Initializes the responder.
	 * @throws InitException if the responder cannot be created.
	 */
	@Override
	public void init() throws InitException {

		// Set the key.
		key = KEY + "-" + name;
		
		// Init with the responder identity.
		impl.init(StringId.from(key, This.getId()));

		// Store the responder data.
		JSONObject jsonData = new JSONObject();
		jsonData.put(PORT, impl.getResponderPort());
		
		try {
			This.getCom().storeKeyValue(key, jsonData.toJSONString());
		}
		catch (KeyAlreadyExistsException e) {
			impl.terminate();
			impl = null;
			throw new InitException("A responder with the name \"" + name + "\" already exists");
		}
		
		setReady();
	}
		
	/**
	 * Gets the name.
	 * @return The name. 
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Receives a request. This is a blocking command until a Request is received.
	 * @return A Request object.
	 */
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

	/**
	 * Cancels the responder waiting in another thread.
	 */
	@Override
	public void cancel() {
		impl.cancel();			
	}
	
	/**
	 * Returns true if it has been canceled.
	 * @return True if canceled.
	 */
	@Override
	public boolean isCanceled() {
		return impl.isCanceled();
	}
	
	/**
	 * Terminates the communication.
	 */
	@Override
	public void terminate() {
		
		if (impl != null) {
			try {
				This.getCom().removeKey(key);
			}
			catch (UndefinedKeyException e) {
				// No need to treat.
			}
			
			waiting.remove();
			impl.terminate();
		}
		
		setTerminated();
	}

	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("type", "basic-responder");
		result.put("name", name);
		result.put("app", new AppIdentity(This.getName(), This.getId(), new ServerIdentity(This.getEndpoint().toString(), false)).toJSON());
		
		return result.toJSONString();
	}
	
}
