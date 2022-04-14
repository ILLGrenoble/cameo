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
import fr.ill.ics.cameo.base.IObject;
import fr.ill.ics.cameo.base.KeyValueGetterException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.StringId;

/**
 * Class defining a subscriber.
 */
public class Subscriber implements IObject {
	
	private App app;
	private String publisherName;
	private boolean useProxy = false;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private SubscriberImpl impl;
	private SubscriberWaiting waiting = new SubscriberWaiting(this);
	private String key;
	
	private Subscriber(App app, String publisherName) {
		this.app = app;
		this.publisherName = publisherName;
		this.impl = ImplFactory.createSubscriber();
		waiting.add();
	}
	
	private void synchronize(App app) throws RequesterCreationException {
		
		Requester requester = Requester.create(app, Publisher.RESPONDER_PREFIX + publisherName);
		requester.init();
		
		// Send a subscribe request.
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Publisher.SUBSCRIBE_PUBLISHER);
		
		requester.sendString(jsonRequest.toJSONString());
		String response = requester.receiveString();
		
		requester.terminate();
	}
	
	/**
	 * Returns a new subscriber.
	 * @param app The application where the publisher is defined.
	 * @param publisherName The name of the publisher.
	 * @return A new Subscriber object.
	 */
	public static Subscriber create(App app, String publisherName) throws SubscriberCreationException {
		return new Subscriber(app, publisherName);
	}
	
	/**
	 * Initializes the subscriber.
	 * @throws SubscriberCreationException if the subscriber cannot be created.
	 */
	@Override
	public void init() throws SubscriberCreationException {
		
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		this.key = Publisher.KEY + "-" + publisherName;
		this.useProxy = app.usesProxy();
		
		// Get the publisher data.
		try {
			KeyValueGetter getter = app.getCom().getKeyValueGetter(key);
			String jsonString = getter.get();
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
			
			impl.init(appId, endpoint, app.getStatusEndpoint(), StringId.from(appId, key));
	
			// Synchronize the subscriber only if the number of subscribers > 0.
			if (numberOfSubscribers > 0) {
				synchronize(app);
			}
		}
		catch (KeyValueGetterException | RequesterCreationException e) {
			throw new SubscriberCreationException("Cannot create subscriber");
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
	 * Returns true if the subscriber has been canceled.
	 * @return True if the subscriber has been canceled.
	 */
	public boolean isCanceled() {
		return impl.isCanceled();
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
	
	/**
	 * Cancels the subscriber. Unblocks the receive() call in another thread.
	 */
	public void cancel() {
		impl.cancel();
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
		return "sub." + publisherName + ":" + appName + "." + appId + "@" + appEndpoint;
	}
}