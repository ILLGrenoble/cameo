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

package fr.ill.ics.cameo.coms.multi;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.ICancelable;
import fr.ill.ics.cameo.base.IObject;
import fr.ill.ics.cameo.base.InitException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.multi.impl.ResponderImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.Messages;

/**
 * Class defining a responder for the responder router.
 * Requests are processed sequentially.
 */
public class Responder implements IObject, ICancelable {
	
	private String dealerEndpoint;
	private ResponderImpl impl;
	
	private Responder(String dealerEndpoint) {
		this.dealerEndpoint = dealerEndpoint;
		this.impl = ImplFactory.createMultiResponder();
	}

	/**
	 * Creates a new responder.
	 * @param router The router.
	 * @return A new Responder object.
	 */
	static public Responder create(ResponderRouter router) {
		return new Responder(router.getDealerEndpoint());
	}
	
	/**
	 * Initializes the responder.
	 */
	@Override
	public void init() {

		// Init with the responder socket.
		impl.init(dealerEndpoint);
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
		
		//waiting.remove();
		impl.terminate();
	}

	@Override
	public String toString() {
		return "repm:" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
	
}
