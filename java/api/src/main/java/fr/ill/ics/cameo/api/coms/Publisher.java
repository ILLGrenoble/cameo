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

package fr.ill.ics.cameo.api.coms;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.api.base.ICancelable;
import fr.ill.ics.cameo.api.base.InitException;
import fr.ill.ics.cameo.api.base.KeyAlreadyExistsException;
import fr.ill.ics.cameo.api.base.StateObject;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.base.UndefinedKeyException;
import fr.ill.ics.cameo.api.coms.basic.Request;
import fr.ill.ics.cameo.api.coms.basic.Responder;
import fr.ill.ics.cameo.api.coms.impl.PublisherImpl;
import fr.ill.ics.cameo.api.factory.ImplFactory;
import fr.ill.ics.cameo.common.messages.JSON;
import fr.ill.ics.cameo.common.messages.Messages;
import fr.ill.ics.cameo.common.strings.AppIdentity;
import fr.ill.ics.cameo.common.strings.ServerIdentity;
import fr.ill.ics.cameo.common.strings.StringId;

/**
 * Class defining a publisher. It can be synchronized with a certain number of subscribers or not.
 */
public class Publisher extends StateObject implements ICancelable {

	private String name;
	private int numberOfSubscribers;
	private PublisherImpl impl;
	private PublisherWaiting waiting = new PublisherWaiting(this);
	private String key;
	private Responder responder = null;
	private AtomicBoolean canceled = new AtomicBoolean(false);
	
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
	
	/**
	 * Initializes the publisher.
	 * @throws InitException if the publisher cannot be initialized.
	 */
	@Override
	public void init() throws InitException {
		
		if (isReady()) {
			// The object is already initialized.
			return;
		}
		
		// Set the key.
		key = KEY + "-" + name;
		
		// Init the publisher and synchronizer sockets.
		impl.init(StringId.from(key, This.getId()));
		
		// Store the publisher data.
		JSONObject jsonData = new JSONObject();
		jsonData.put(PUBLISHER_PORT, impl.getPublisherPort());
		jsonData.put(NUMBER_OF_SUBSCRIBERS, numberOfSubscribers);
		
		try {
			This.getCom().storeKeyValue(key, jsonData.toJSONString());
		}
		catch (KeyAlreadyExistsException e) {
			impl.terminate();
			impl = null;
			throw new InitException("A publisher with the name \"" + name + "\" already exists");
		}
		
		// Wait for the subscribers.
		if (numberOfSubscribers > 0) {
			waitForSubscribers();
		}
		
		setReady();
	}

	/**
	 * Returns the publisher with name.
	 * @param name The name.
	 * @param numberOfSubscribers The number of subscribers.
	 * @return A new Publisher object.
	 */
	static public Publisher create(String name, int numberOfSubscribers) {
		return new Publisher(name, numberOfSubscribers);
	}

	/**
	 * Returns the publisher with name.
	 * @param name The name.
	 * @return A new Publisher object.
	 */
	static public Publisher create(String name) {
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
		catch (InitException e) {
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
	@Override
	public void cancel() {
		if (responder != null) {
			canceled.set(true);
			responder.cancel();
		}
	}
	
	/**
	 * Returns true if is canceled.
	 * @return True if is canceled.
	 */
	@Override
	public boolean isCanceled() {
		return canceled.get();
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
		
		result.put("type", "publisher");
		result.put("name", name);
		result.put("app", new AppIdentity(This.getName(), This.getId(), new ServerIdentity(This.getEndpoint().toString(), false)).toJSON());
		
		return result.toJSONString();
	}
}