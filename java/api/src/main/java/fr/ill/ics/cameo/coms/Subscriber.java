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
import fr.ill.ics.cameo.base.KeyValueGetterException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.StringId;

/**
 * Class Subscriber. 
 *
 */
public class Subscriber {
	
	private boolean useProxy = false;
	private String publisherName;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private SubscriberImpl impl;
	private SubscriberWaiting waiting = new SubscriberWaiting(this);
	private String key;
	
	private Subscriber() {
		this.impl = ImplFactory.createSubscriber();
		waiting.add();
	}
	
	private void synchronize(App app) throws RequesterCreationException {
		
		Requester requester = Requester.create(app, Publisher.RESPONDER_PREFIX + publisherName);

		// Send a subscribe request.
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Publisher.SUBSCRIBE_PUBLISHER);
		
		requester.sendString(jsonRequest.toJSONString());
		String response = requester.receiveString();
		
		requester.terminate();
	}
	
	private void init(App app, String publisherName) throws SubscriberCreationException {
		
		this.publisherName = publisherName;
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
	 * Subscribes to the application publisher.
	 * @param publisherName
	 * @return
	 * @throws SubscriberCreationException 
	 */
	public static Subscriber create(App app, String publisherName) throws SubscriberCreationException {
		
		Subscriber subscriber = new Subscriber();
		subscriber.init(app, publisherName);
		
		return subscriber;
	}
			
	public String getPublisherName() { 
		return publisherName;
	}
	
	public String getAppName() {
		return appName;
	}
	
	public int getAppId() {
		return appId;
	}

	public Endpoint getAppEndpoint() {
		return appEndpoint;
	}
		
	public boolean isEnded() {
		return impl.isEnded();
	}
	
	public boolean isCanceled() {
		return impl.isCanceled();
	}
			
	/**
	 * 
	 * @return the byte[] data. If the return value is null, then the stream is finished. 
	 */
	public byte[] receive() {
		return impl.receive();
	}
	
	/**
	 * 
	 * @return the string data. If the return value is null, then the stream is finished. 
	 */
	public String receiveString() {
		return impl.receiveString();
	}
	
	/**
	 * 
	 * @return the two parts byte[][] data. If the return value is null, then the stream is finished. 
	 */
	public byte[][] receiveTwoParts() {
		return impl.receiveTwoParts();
	}
	
	public void cancel() {
		impl.cancel();
	}
	
	public void terminate() {
		waiting.remove();
		impl.terminate();
	}

	@Override
	public String toString() {
		return "sub." + publisherName + ":" + appName + "." + appId + "@" + appEndpoint;
	}
}