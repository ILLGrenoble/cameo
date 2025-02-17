/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.base;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import eu.ill.cameo.api.base.impl.RequestSocketImpl;
import eu.ill.cameo.api.factory.ImplFactory;
import eu.ill.cameo.common.messages.JSON.Parser;
import eu.ill.cameo.common.messages.Messages;

/**
 * Class defining a request socket.
 */
public class RequestSocket {

	private RequestSocketImpl impl;
	private Parser parser;

	/**
	 * Constructor.
	 * @param context The context.
	 * @param endpoint The endpoint.
	 * @param responderIdentity The responder identity.
	 * @param timeout The timeout.
	 * @param parser The parser.
	 */
	public RequestSocket(Context context, String endpoint, String responderIdentity, int timeout, Parser parser) {
		this.impl = ImplFactory.createRequestSocket(context, endpoint, responderIdentity, timeout);
		this.parser = parser;
	}
	
	/**
	 * Sets the timeout.
	 * @param timeout The timeout.
	 */
	public void setTimeout(int timeout) {
		this.impl.setTimeout(timeout);
	}
	
	/**
	 * Send a request.
	 * @param request The request.
	 * @param overrideTimeout Timeout that overrides the timeout defined previously.
	 * @return The response.
	 */
	public byte[][] request(byte[] request) {
		 return impl.request(request, -1);
	}
	
	/**
	 * Send a request.
	 * @param requestPart1 The request part 1.
	 * @param requestPart2 The request part 2.
	 * @return The response.
	 */
	public byte[][] request(byte[] requestPart1, byte[] requestPart2) {
		 return impl.request(requestPart1, requestPart2, -1);
	}
	
	/**
	 * Send a request.
	 * @param requestPart1 The request part 1.
	 * @param requestPart2 The request part 2.
	 * @param requestPart3 The request part 3.
	 * @return The response.
	 */
	public byte[][] request(byte[] requestPart1, byte[] requestPart2, byte[] requestPart3) {
		 return impl.request(requestPart1, requestPart2, requestPart3, -1);
	}
	
	/**
	 * Send a request.
	 * @param request The JSON string request.
	 * @param timeout Timeout that overrides the timeout defined previously.
	 * @return The JSON object response.
	 */
	public JSONObject requestJSON(JSONObject request, int timeout) throws ConnectionTimeout {
		
		byte[][] reply = impl.request(Messages.serialize(request), timeout);
		
		try {
			return parser.parse(Messages.parseString(reply[0]));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	/**
	 * Send a request.
	 * @param request The JSON string request.
	 * @param timeout Timeout that overrides the timeout defined previously.
	 * @return The JSON object response.
	 */
	public JSONObject requestJSON(JSONObject request) throws ConnectionTimeout {
		return requestJSON(request, -1);
	}
	
	/**
	 * Send a request.
	 * @param request The JSON string request.
	 * @param data The binary data.
	 * @param timeout Timeout that overrides the timeout defined previously.
	 * @return The JSON object response.
	 */
	public JSONObject requestJSON(JSONObject request, byte[] data, int timeout) throws ConnectionTimeout {
		
		byte[][] reply = impl.request(Messages.serialize(request), data, timeout);
		
		try {
			return parser.parse(Messages.parseString(reply[0]));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	/**
	 * Send a request.
	 * @param request The JSON string request.
	 * @param data The binary data.
	 * @return The JSON object response.
	 */
	public JSONObject requestJSON(JSONObject request, byte[] data) throws ConnectionTimeout {
		return requestJSON(request, data, -1);
	}
	
	/**
	 * Terminates the communications.
	 */
	public void terminate() {
		impl.terminate();
	}
}