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

package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.KeyAlreadyExistsException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.basic.Request;
import fr.ill.ics.cameo.coms.basic.Responder;
import fr.ill.ics.cameo.coms.impl.PublisherImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.StringId;

/**
 * Class defining a publisher. It can be synchronized with a certain number of subscribers or not.
 */
public class Publisher {

	private String name;
	private int numberOfSubscribers;
	private PublisherImpl impl;
	private PublisherWaiting waiting = new PublisherWaiting(this);
	private String key;
	private Responder responder = null;
	private boolean canceled = false;
	
	public static final String KEY = "publisher-55845880-56e9-4ad6-bea1-e84395c90b32";
	public static final String PUBLISHER_PORT = "publisher_port";
	public static final String NUMBER_OF_SUBSCRIBERS = "n_subscribers";
	public static final String RESPONDER_PREFIX = "publisher:";
	public static final long SUBSCRIBE_PUBLISHER = 100;
	
	private Publisher(String name, int numberOfSubscribers) {
		
		this.name = name;
		this.numberOfSubscribers = numberOfSubscribers;
		
		this.impl = ImplFactory.createPublisher();
		
		waiting.add();
	}
	
	public void init() throws PublisherCreationException {
		
		// Set the key.
		key = KEY + "-" + name;
		
		// Init the publisher and synchronizer sockets.
		impl.init(StringId.from(This.getId(), key));
		
		// Store the publisher data.
		JSONObject jsonData = new JSONObject();
		jsonData.put(PUBLISHER_PORT, impl.getPublisherPort());
		jsonData.put(NUMBER_OF_SUBSCRIBERS, numberOfSubscribers);
		
		try {
			This.getCom().storeKeyValue(key, jsonData.toJSONString());
		}
		catch (KeyAlreadyExistsException e) {
			throw new PublisherCreationException("A publisher with the name \"" + name + "\" already exists");
		}
		
		// Wait for the subscribers.
		if (numberOfSubscribers > 0) {
			waitForSubscribers();
		}
	}

	/**
	 * Returns the publisher with name.
	 * @param name The name.
	 * @param numberOfSubscribers The number of subscribers.
	 * @return A new Publisher object.
	 */
	static public Publisher create(String name, int numberOfSubscribers) throws PublisherCreationException {
		return new Publisher(name, numberOfSubscribers);
	}

	/**
	 * Returns the publisher with name.
	 * @param name The name.
	 * @return A new Publisher object.
	 */
	static public Publisher create(String name) throws PublisherCreationException {
		return create(name, 0);
	}

	/**
	 * Gets the name.
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns true if the wait succeeds or false if it was canceled.
	 * @return True if the wait succeeds or false if it was canceled.
	 */
	private boolean waitForSubscribers() {
		
		try {
			// Create the responder.
			responder = Responder.create(RESPONDER_PREFIX + name);
			responder.init();
		
			// Loop until the number of subscribers is reached.
			int counter = 0;
			
			while (counter < numberOfSubscribers) {
				
				Request request = responder.receive();
				
				if (request == null) {
					return false;
				}
				
				// Get the JSON request object.
				JSONObject jsonRequest = This.getCom().parse(request.get());
				
				// Get the type.
				long type = JSON.getLong(jsonRequest, Messages.TYPE);
				
				if (type == SUBSCRIBE_PUBLISHER) {
					counter++;
				}
				
				request.replyString("OK");
			}
			
			return !responder.isCanceled();
		}
		catch (ResponderCreationException e) {
			return false;
		}
		finally {
			// Destroy responder.
			if (responder != null) {
				responder.terminate();
				responder = null;
			}
		}
	}
	
	/**
	 * Cancels the init() call in another thread.
	 */
	public void cancel() {
		if (responder != null) {
			canceled = true;
			responder.cancel();
		}
	}
	
	/**
	 * Returns true if is canceled.
	 * @return True if is canceled.
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Sends a message in one binary part.
	 * @param data The data to send.
	 */
	public void send(byte[] data) {
		impl.send(data);
	}
	
	/**
	 * Sends a message in one string part.
	 * @param data The data to send.
	 */
	public void sendString(String data) {
		impl.send(data);
	}
	
	/**
	 * Sends a message in two binary parts.
	 * \param data1 The first part.
	 * \param data2 The second part.
	 */		
	public void sendTwoParts(byte[] data1, byte[] data2) {
		impl.sendTwoParts(data1, data2);
	}
	
	/**
	 * Sends the end of the stream.
	 */
	public void sendEnd() {
		impl.sendEnd();
	}
	
	/**
	 * Returns true if the stream ended.
	 * @return True if the stream ended.
	 */
	public boolean hasEnded() {
		return impl.hasEnded();
	}
	
	/**
	 * Terminates the communication.
	 */
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
		return "pub." + getName() + ":" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
}
