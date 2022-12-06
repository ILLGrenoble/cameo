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

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.App.Com.KeyValueGetter;
import fr.ill.ics.cameo.base.ICancelable;
import fr.ill.ics.cameo.base.IObject;
import fr.ill.ics.cameo.base.ITimeoutable;
import fr.ill.ics.cameo.base.InitException;
import fr.ill.ics.cameo.base.KeyValueGetterException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.Timeout;
import fr.ill.ics.cameo.base.TimeoutCounter;
import fr.ill.ics.cameo.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.AppIdentity;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.ServerIdentity;
import fr.ill.ics.cameo.strings.StringId;

/**
 * Class defining a subscriber.
 */
public class Subscriber implements IObject, ITimeoutable, ICancelable {
	
	private App app;
	private String publisherName;
	private int timeout = -1;
	private boolean useProxy = false;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private SubscriberImpl impl;
	private SubscriberWaiting waiting = new SubscriberWaiting(this);
	private String key;
	private KeyValueGetter getter;
	
	private Subscriber(App app, String publisherName) {
		this.app = app;
		this.publisherName = publisherName;
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		this.key = Publisher.KEY + "-" + publisherName;
		this.useProxy = app.usesProxy();
		this.impl = ImplFactory.createSubscriber();
		waiting.add();
		this.getter = app.getCom().createKeyValueGetter(key);
	}
	
	private void synchronize(App app, TimeoutCounter timeoutCounter) {
		
		Requester requester = Requester.create(app, Publisher.RESPONDER_PREFIX + publisherName);
		
		// Set the timeout that can be -1.
		requester.setTimeout(timeoutCounter.remains());
		
		// A Timeout exception may be thrown.
		requester.init();
		
		// Set the timeout again because init() may have taken time.
		requester.setTimeout(timeoutCounter.remains());
		
		// Send a subscribe request.
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Publisher.SUBSCRIBE_PUBLISHER);
		
		requester.sendString(jsonRequest.toJSONString());
		String response = requester.receiveString();
		
		requester.terminate();
		
		// Check timeout.
		if (requester.hasTimedout()) {
			throw new Timeout("Timeout while synchronizing subscriber");
		}
	}
	
	/**
	 * Returns a new subscriber.
	 * @param app The application where the publisher is defined.
	 * @param publisherName The name of the publisher.
	 * @return A new Subscriber object.
	 */
	public static Subscriber create(App app, String publisherName) {
		return new Subscriber(app, publisherName);
	}
	
	/**
	 * Initializes the subscriber.
	 * @throws InitException if the subscriber cannot be created.
	 */
	@Override
	public void init() throws InitException {
		
		// Get the publisher data.
		try {
			TimeoutCounter timeoutCounter = new TimeoutCounter(timeout);
			
			String jsonString = getter.get(timeoutCounter);
			JSONObject jsonData = This.getCom().parse(jsonString);
			int numberOfSubscribers = JSON.getInt(jsonData, Publisher.NUMBER_OF_SUBSCRIBERS);
			
			Endpoint endpoint;
			
			// The endpoint depends on the use of the proxy.
			if (useProxy) {
				int publisherPort = app.getCom().getPublisherProxyPort();
				endpoint = app.getEndpoint().withPort(publisherPort);
			}
			else {
				int publisherPort = JSON.getInt(jsonData, Publisher.PUBLISHER_PORT);
				endpoint = app.getEndpoint().withPort(publisherPort);
			}
			
			impl.init(appId, endpoint, app.getStatusEndpoint(), StringId.from(key, appId));
	
			// Synchronize the subscriber only if the number of subscribers > 0.
			if (numberOfSubscribers > 0) {
				synchronize(app, timeoutCounter);
			}
		}
		catch (Exception e) {
			throw new InitException("Cannot create subscriber: " + e.getMessage());
		}
	}
	
	/**
	 * Gets the publisher name.
	 * @return The publisher name.
	 */
	public String getPublisherName() { 
		return publisherName;
	}
	
	/**
	 * Gets the application name.
	 * @return The application name.
	 */
	public String getAppName() {
		return appName;
	}
	
	/**
	 * Gets the application id.
	 * @return The application id.
	 */
	public int getAppId() {
		return appId;
	}

	/**
	 * Gets the application endpoint.
	 * @return The application endpoint.
	 */
	public Endpoint getAppEndpoint() {
		return appEndpoint;
	}
	
	/**
	 * Returns true if the stream ended.
	 * @return True if the stream ended.
	 */
	public boolean hasEnded() {
		return impl.hasEnded();
	}

	/**
	 * Returns the binary data nothing if the stream has finished.
	 * @return The binary data or null.
	 */
	public byte[] receive() {
		return impl.receive();
	}
	
	/**
	 * Returns the string data nothing if the stream has finished.
	 * @return The string data or null.
	 */
	public String receiveString() {
		return impl.receiveString();
	}
	
	/**
	 * Returns an array of bytes or nothing if the stream has finished.
	 * @return The array of bytes or null.
	 */
	public byte[][] receiveTwoParts() {
		return impl.receiveTwoParts();
	}

	@Override
	public void setTimeout(int value) {
		timeout = value;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}
	
	/**
	 * Cancels the subscriber. Unblocks the receive() call in another thread.
	 */
	@Override
	public void cancel() {
		impl.cancel();
	}
	
	/**
	 * Returns true if the subscriber has been canceled.
	 * @return True if the subscriber has been canceled.
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
		waiting.remove();
		impl.terminate();
	}

	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("type", "subscriber");
		result.put("name", publisherName);
		result.put("app", new AppIdentity(appName, appId, new ServerIdentity(appEndpoint.toString(), useProxy)).toJSON());
		
		return result.toJSONString();
	}
}