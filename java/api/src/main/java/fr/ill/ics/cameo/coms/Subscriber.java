package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.KeyValue;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedApplicationException;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.basic.Requester;
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
	
	private void tryInit(Instance app) throws SubscriberCreationException {
		
		// Memorize proxy.
		useProxy = app.usesProxy();
		
		// Get the publisher data.
		try {
			String jsonString = app.getCom().getKeyValue(key);
			
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
		catch (UndefinedApplicationException | UndefinedKeyException e) {
			throw new SubscriberCreationException("");
		}
	}
	
	private void synchronize(Instance app) {
		
		try {
			Requester requester = Requester.create(app, Publisher.RESPONDER_PREFIX + publisherName);

			// Send a subscribe request.
			JSONObject jsonRequest = new JSONObject();
			jsonRequest.put(Messages.TYPE, Publisher.SUBSCRIBE_PUBLISHER);
			
			requester.send(jsonRequest.toJSONString());
			String response = requester.receiveString();
			
			requester.terminate();
		}
		catch (RequesterCreationException e) {
			System.err.println("Error, cannot create requester for subscriber");
		}
	}
	
	private boolean init(Instance app, String publisherName) {
		
		this.publisherName = publisherName;
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		this.key = Publisher.KEY + "-" + publisherName;
		
		// Try to create the subscriber.
		// If the publisher does not exist, an exception is thrown.
		try {
			tryInit(app);
			return true;
		}
		catch (SubscriberCreationException e) {
			// The publisher does not exist, so we are waiting for it.
		}

		// Wait for the publisher.
		int lastState = app.waitFor(new KeyValue(key));
		
		// The state cannot be terminal or it means that the application has terminated that is not planned.
		if (lastState == Application.State.SUCCESS 
			|| lastState == Application.State.STOPPED
			|| lastState == Application.State.KILLED					
			|| lastState == Application.State.ERROR) {
			return false;
		}
		
		// The subscriber can be created.
		try {
			tryInit(app);
			return true;
		}
		catch (SubscriberCreationException e) {
			// That should not happen.
			System.err.println("the publisher " + publisherName + " does not exist but should");
		}
		
		return false;
	}
	
	/**
	 * Subscribes to the application publisher.
	 * @param publisherName
	 * @return
	 */
	public static Subscriber create(Instance app, String publisherName) {
		
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