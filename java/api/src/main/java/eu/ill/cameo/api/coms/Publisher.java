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

package eu.ill.cameo.api.coms;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.ICancelable;
import eu.ill.cameo.api.base.InitException;
import eu.ill.cameo.api.base.KeyAlreadyExistsException;
import eu.ill.cameo.api.base.StateObject;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.UndefinedKeyException;
import eu.ill.cameo.api.coms.basic.Request;
import eu.ill.cameo.api.coms.basic.Responder;
import eu.ill.cameo.api.coms.impl.PublisherImpl;
import eu.ill.cameo.api.factory.ImplFactory;
import eu.ill.cameo.common.messages.JSON;
import eu.ill.cameo.common.messages.Messages;
import eu.ill.cameo.common.strings.AppIdentity;
import eu.ill.cameo.common.strings.ServerIdentity;
import eu.ill.cameo.common.strings.StringId;

/**
 * Class defining a publisher. It can be synchronized with a certain number of subscribers or not.
 */
public class Publisher extends StateObject implements ICancelable {

	private String name;
	private int numberOfSubscribers = 0;
	private boolean syncSubscribers = false;
	private PublisherImpl impl = ImplFactory.createPublisher(false);
	private PublisherWaiting waiting = new PublisherWaiting(this);
	private String key;
	private Responder responder = null;
	private Thread responderThread = null;
	private LinkedBlockingQueue<Long> responderQueue = new LinkedBlockingQueue<>();
	private AtomicBoolean canceled = new AtomicBoolean(false);
	
	public static final String KEY = "publisher-55845880-56e9-4ad6-bea1-e84395c90b32";
	public static final String PUBLISHER_PORT = "publisher_port";
	public static final String NUMBER_OF_SUBSCRIBERS = "n_subscribers";
	public static final String SYNC_SUBSCRIBERS = "sync_subscribers";
	public static final String RESPONDER_PREFIX = "publisher:";
	public static final long SUBSCRIBE_PUBLISHER = 100;
	private static final long CANCEL_RESPONDER = 0;
	
	private Publisher(String name) {
		
		this.name = name;
		waiting.add();
	}

	/**
	 * Sets the subscribers synchronized. By default, the subscribers are not synchronized.
	 * @param value True if synchronized.
	 */
	public void setSyncSubscribers(boolean value) {
		this.syncSubscribers = value;
	}
	
	/**
	 * Sets the wait for subscribers. If set then the subscribers are set synchronized.
	 * @param numberOfSubscribers The number of subscribers.
	 */
	public void setWaitForSubscribers(int numberOfSubscribers) {
		this.syncSubscribers = true;
		this.numberOfSubscribers = numberOfSubscribers;
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
		
		// Replace the implementation if sync.
		this.impl = ImplFactory.createPublisher(syncSubscribers);
		
		// Set the key.
		key = KEY + "-" + name;
		
		// Init the publisher and synchronizer sockets.
		impl.init(StringId.from(key, This.getId()));
		
		// Store the publisher data.
		JSONObject jsonData = new JSONObject();
		jsonData.put(PUBLISHER_PORT, impl.getPublisherPort());
		jsonData.put(NUMBER_OF_SUBSCRIBERS, numberOfSubscribers);
		jsonData.put(SYNC_SUBSCRIBERS, syncSubscribers);
		
		try {
			This.getCom().storeKeyValue(key, jsonData.toJSONString());
		}
		catch (KeyAlreadyExistsException e) {
			impl.terminate();
			impl = null;
			throw new InitException("A publisher with the name \"" + name + "\" already exists");
		}
		
		// Create the responder thread if subscribers are synchronized or waiting for subscribers is enabled.
		if (numberOfSubscribers > 0 || syncSubscribers) {
			
			try {
				// Create the responder.
				responder = Responder.create(RESPONDER_PREFIX + name);
				responder.init();
			
				responderThread = new Thread(new Runnable() {
					public void run() {
						
						while (true) {
							
							Request request = responder.receive();
							
							if (request == null) {
								return;
							}
							
							// Get the JSON request object.
							JSONObject jsonRequest = This.getCom().parse(request.get());
							
							// Get the type.
							long type = JSON.getLong(jsonRequest, Messages.TYPE);
							
							if (type == SUBSCRIBE_PUBLISHER) {
								try {
									responderQueue.put(Long.valueOf(SUBSCRIBE_PUBLISHER));
								}
								catch (InterruptedException e) {
								}
							}
							else if (type == Messages.SYNC_STREAM) {
								impl.sendSync();
							}
							
							request.replyString("OK");
						}
					}
				});
				
				responderThread.start();
			}
			catch (InitException e) {
				return;
			}
			
			// Wait for the subscribers.
			if (numberOfSubscribers > 0) {
				if (!waitForSubscribers()) {
					return;
				}
			}
		}
		
		setReady();
	}

	/**
	 * Returns the publisher with name.
	 * @param name The name.
	 * @return A new Publisher object.
	 */
	static public Publisher create(String name) {
		return new Publisher(name);
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
		
		// Loop until the number of subscribers is reached.
		int counter = 0;
		
		while (counter < numberOfSubscribers) {
			try {
				Long item = responderQueue.take();
							
				if (item.longValue() == SUBSCRIBE_PUBLISHER) {
					counter++;
				}
				else if (item.longValue() == CANCEL_RESPONDER) {
					return false;
				}
			}
			catch (InterruptedException e) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Cancels the init() call in another thread.
	 */
	@Override
	public void cancel() {
		if (responder != null) {
			canceled.set(true);
			responder.cancel();
			
			try {
				responderQueue.put(Long.valueOf(CANCEL_RESPONDER));
			}
			catch (InterruptedException e) {
			}
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
			
			if (responderThread != null) {
				responder.cancel();
				
				try {
					responderThread.join();
				}
				catch (InterruptedException e) {
				}
				responder.terminate();
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
