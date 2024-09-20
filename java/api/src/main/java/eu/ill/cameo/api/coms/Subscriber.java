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

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.api.base.App;
import fr.ill.ics.cameo.api.base.App.Com.KeyValueGetter;
import fr.ill.ics.cameo.api.base.ConnectionTimeout;
import fr.ill.ics.cameo.api.base.ICancelable;
import fr.ill.ics.cameo.api.base.ITimeoutable;
import fr.ill.ics.cameo.api.base.InitException;
import fr.ill.ics.cameo.api.base.StateObject;
import fr.ill.ics.cameo.api.base.SynchronizationTimeout;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.base.Timeout;
import fr.ill.ics.cameo.api.base.TimeoutCounter;
import fr.ill.ics.cameo.api.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.api.factory.ImplFactory;
import fr.ill.ics.cameo.common.messages.JSON;
import fr.ill.ics.cameo.common.messages.Messages;
import fr.ill.ics.cameo.common.strings.AppIdentity;
import fr.ill.ics.cameo.common.strings.Endpoint;
import fr.ill.ics.cameo.common.strings.ServerIdentity;
import fr.ill.ics.cameo.common.strings.StringId;

/**
 * Class defining a subscriber.
 */
public class Subscriber extends StateObject implements ITimeoutable, ICancelable {
	
	private App app;
	private String publisherName;
	private boolean checkApp = false;
	private int timeout = -1;
	private boolean useProxy = false;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private SubscriberImpl impl;
	private SubscriberWaiting waiting = new SubscriberWaiting(this);
	private String key;
	private KeyValueGetter keyValueGetter;
	private Requester requester;
	
	private static int SYNC_TIMEOUT = 100;
	
	private Subscriber(App app, String publisherName) {
		this.app = app;
		this.publisherName = publisherName;
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		this.key = Publisher.KEY + "-" + publisherName;
		this.useProxy = app.usesProxy();
		this.impl = ImplFactory.createSubscriber();
		this.waiting.add();
		this.keyValueGetter = app.getCom().createKeyValueGetter(key);
	}
	
	private void synchronize(TimeoutCounter timeoutCounter, int numberOfSubscribers, boolean syncSubscribers) {

		// Create the requester.
		requester = Requester.create(app, Publisher.RESPONDER_PREFIX + publisherName);
		requester.setCheckApp(checkApp);
		
		// Set the timeout that can be -1.
		requester.setTimeout(timeoutCounter.remains());
		
		// A Timeout exception may be thrown.
		requester.init();
		
		// Set the timeout again because init() may have taken time.
		requester.setTimeout(timeoutCounter.remains());
		
		// Check timeout.
		boolean timedOut = false;
	
		// Check sync subscribers.
		if (syncSubscribers) {
			
			int syncTimeout = 0;

			while (!timedOut) {
				
				// Send a sync request.
				JSONObject jsonRequest = new JSONObject();
				jsonRequest.put(Messages.TYPE, Messages.SYNC_STREAM);
				
				requester.sendString(jsonRequest.toJSONString());
				String response = requester.receiveString();
				
				syncTimeout += SYNC_TIMEOUT;
				
				// Check subscriber.
				if (impl.sync(syncTimeout)) {
					break;
				}

				// Check timeout.
				timedOut = requester.hasTimedout();
			}
		}
		
		// Send subscription.
		if (!timedOut) {		
			
			// Check number of subscribers.
			if (numberOfSubscribers > 0) {
				// Send a subscribe request.
				JSONObject jsonRequest = new JSONObject();
				jsonRequest.put(Messages.TYPE, Publisher.SUBSCRIBE_PUBLISHER);
				
				requester.sendString(jsonRequest.toJSONString());
				String response = requester.receiveString();
			}
		}
		
		// Check timeout.
		timedOut = requester.hasTimedout();
		
		// Terminate the requester as it is not used any more.
		requester.terminate();
		
		if (timedOut) {
			throw new Timeout();
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
	 * A thread is checking the state of the app and cancels the requester if it fails.
	 * @param value True if the app is checked.
	 */
	public void setCheckApp(boolean value) {
		this.checkApp = value;
	}
	
	/**
	 * Initializes the subscriber.
	 * @throws InitException if the subscriber cannot be created.
	 * @throws SynchronizationTimeout if the subscriber cannot synchronize the publisher.
	 */
	@Override
	public void init() throws InitException {
		
		if (isReady()) {
			// The object is already initialized.
			return;
		}
		
		// Get the publisher data.
		try {
			TimeoutCounter timeoutCounter = new TimeoutCounter(timeout);
			
			String jsonString = keyValueGetter.get(timeoutCounter);
			
			if (keyValueGetter.isCanceled()) {
				return;
			}
			
			JSONObject jsonData = This.getCom().parse(jsonString);
			int numberOfSubscribers = JSON.getInt(jsonData, Publisher.NUMBER_OF_SUBSCRIBERS);
			boolean syncSubscribers = JSON.getBoolean(jsonData, Publisher.SYNC_SUBSCRIBERS);
			
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
			
			impl.init(appId, endpoint, app.getStatusEndpoint(), StringId.from(key, appId), checkApp);
	
			// Synchronize the subscriber only if the number of subscribers > 0.
			if (numberOfSubscribers > 0 || syncSubscribers) {
				synchronize(timeoutCounter, numberOfSubscribers, syncSubscribers);
			}
		}
		catch (ConnectionTimeout e) {
			throw e;
		}
		catch (Timeout e) {
			throw new SynchronizationTimeout("Subscriber cannot synchronize publisher '" + publisherName + "'");
		}
		catch (SynchronizationTimeout e) {
			throw new SynchronizationTimeout("Subscriber cannot synchronize publisher '" + publisherName + "'");
		}
		catch (InitException e) {
			throw new InitException("Cannot initialize subscriber to publisher '" + publisherName + "': Cannot initialize internal requester");
		}
		catch (Exception e) {
			throw new InitException("Cannot initialize subscriber to publisher '" + publisherName + "':" + e.getMessage());
		}
		
		setReady();
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
		impl.setTimeout(value);
	}

	/**
	 * Sets the polling time.
	 * @param value The value.
	 */
	void setPollingTime(int value) {
		impl.setPollingTime(value);
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
		keyValueGetter.cancel();
		
		if (requester != null) {
			requester.cancel();
		}
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
	 * Returns true if the subscriber has timed out.
	 * @return True if the subscriber has timed out.
	 */
	public boolean hasTimedout() {
		return impl.hasTimedout();
	}
	
	/**
	 * Terminates the communication.
	 */
	@Override
	public void terminate() {
		
		if (requester != null) {
			requester.terminate();
			requester = null;
		}
		waiting.remove();
		impl.terminate();
		
		setTerminated();
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