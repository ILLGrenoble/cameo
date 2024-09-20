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

package fr.ill.ics.cameo.api.coms.multi;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.api.base.ICancelable;
import fr.ill.ics.cameo.api.base.IdGenerator;
import fr.ill.ics.cameo.api.base.InitException;
import fr.ill.ics.cameo.api.base.KeyAlreadyExistsException;
import fr.ill.ics.cameo.api.base.StateObject;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.base.UndefinedKeyException;
import fr.ill.ics.cameo.api.coms.multi.impl.ResponderRouterImpl;
import fr.ill.ics.cameo.api.factory.ImplFactory;
import fr.ill.ics.cameo.common.strings.AppIdentity;
import fr.ill.ics.cameo.common.strings.ServerIdentity;
import fr.ill.ics.cameo.common.strings.StringId;

/**
 * Class defining a responder router.
 * Requests are dispatched to the multi responders that process them in parallel.
 */
public class ResponderRouter extends StateObject implements ICancelable {
	
	private String name;
	private ResponderRouterImpl impl;
	private ResponderRouterWaiting waiting = new ResponderRouterWaiting(this);
	private String key;
	private String dealerEndpoint;
	
	public final static String KEY = "responder-676e576d-6102-42d8-ae24-222a7000dfa0";
	public final static String PORT = "port";
	
	private ResponderRouter(String name) {
		this.name = name;
		this.impl = ImplFactory.createMultiResponderRouter();
		
		waiting.add();
	}

	/**
	 * Initializes the responder router.
	 * @throws InitException when the router cannot be initialized.
	 */
	@Override
	public void init() throws InitException {

		if (isReady()) {
			// The object is already initialized.
			return;
		}
		
		// Set the key.
		key = KEY + "-" + name;
		
		// Set the dealer endpoint.
		dealerEndpoint = "inproc://" + IdGenerator.newStringId();
		
		// Init with the responder identity.
		impl.init(StringId.from(key, This.getId()), dealerEndpoint);

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
	 * Returns the responder router with name.
	 * @param name The name.
	 * @return The new ResponderRouter object.
	 */
	static public ResponderRouter create(String name) {
		return new ResponderRouter(name);
	}
	
	/**
	 * Sets the polling time.
	 * @param value The value.
	 */
	public void setPollingTime(int value) {
		impl.setPollingTime(value);
	}
	
	String getDealerEndpoint() {
		return dealerEndpoint;
	}
		
	/**
	 * Returns the name of the responder.
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Cancels the responder router running in another thread.
	 */
	@Override
	public void cancel() {
		impl.cancel();			
	}
	
	/**
	 * Returns true if the responder router has been canceled.
	 * @return True if canceled.
	 */
	@Override
	public boolean isCanceled() {
		return impl.isCanceled();
	}

	/**
	 * Runs the responder router. This is a blocking call.
	 */
	public void run() {
		impl.run();
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
		
		result.put("type", "multi-responder-router");
		result.put("name", name);
		result.put("dealer", dealerEndpoint);
		result.put("app", new AppIdentity(This.getName(), This.getId(), new ServerIdentity(This.getEndpoint().toString(), false)).toJSON());
		
		return result.toJSONString();
	}
	
}
