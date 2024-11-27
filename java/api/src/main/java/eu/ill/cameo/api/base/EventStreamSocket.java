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


import eu.ill.cameo.api.base.impl.EventStreamSocketImpl;
import eu.ill.cameo.api.factory.ImplFactory;
import eu.ill.cameo.common.messages.JSON.Parser;
import eu.ill.cameo.common.strings.Endpoint;

/**
 * Class defining an event stream socket.
 */
public class EventStreamSocket implements ICancelable {
	
	private EventStreamSocketImpl impl;
	
	/**
	 * Constructor.
	 */
	public EventStreamSocket() {
		this.impl = ImplFactory.createEventStreamSocket();
	}

	/**
	 * Initializes the socket.
	 * @param context The context.
	 * @param endpoint The endpoint.
	 * @param requestSocket The request socket.
	 * @param parser The parser.
	 */
	public void init(Context context, Endpoint endpoint, RequestSocket requestSocket, Parser parser) {
		impl.init(context, endpoint, requestSocket, parser);
	}

	/**
	 * Cancels the socket.
	 */
	@Override
	public void cancel() {
		impl.cancel();
	}
	
	/**
	 * Returns true if canceled.
	 * @return True if canceled.
	 */
	@Override
	public boolean isCanceled() {
		return impl.isCanceled();
	}
	
	/**
	 * Receives an Event.
	 * @return An Event object.
	 */
	public Event receive() {
		return impl.receive();
	}
		
	/**
	 * Terminates the socket.
	 */
	public void terminate() {
		impl.terminate();
	}
}