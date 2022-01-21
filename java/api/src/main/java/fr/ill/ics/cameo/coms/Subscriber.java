package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.KeyValue;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedApplicationException;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.coms.impl.zmq.SubscriberZmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

/**
 * Class Subscriber. 
 *
 */
public class Subscriber {
	
	private String publisherName;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private SubscriberImpl impl;
	private SubscriberWaiting waiting = new SubscriberWaiting(this);
	
	private Subscriber() {
		//TODO Replace with factory.
		this.impl = new SubscriberZmq();
		
		waiting.add();
	}
	
	private void initSubscriber(Instance app, String publisherName) throws SubscriberCreationException {
		
		this.publisherName = publisherName;
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		
		// Get the publisher data.
		try {
			String key = Publisher.KEY + "-" + publisherName;
			String jsonString = app.getCom().getKeyValue(key);
			
			JSONObject publisherData = This.getCom().parse(jsonString);
			
			int publisherPort = JSON.getInt(publisherData, Publisher.PUBLISHER_PORT);
			int synchronizerPort = JSON.getInt(publisherData, Publisher.SYNCHRONIZER_PORT);
			int numberOfSubscribers = JSON.getInt(publisherData, Publisher.NUMBER_OF_SUBSCRIBERS);
			
			impl.init(appId, appEndpoint, app.getStatusEndpoint(), publisherPort, synchronizerPort, numberOfSubscribers);	
		}
		catch (UndefinedApplicationException | UndefinedKeyException e) {
			throw new SubscriberCreationException("");
		}
	}
	
	private boolean init(Instance app, String publisherName) {
		
		// Try to create the subscriber.
		// If the publisher does not exist, an exception is thrown.
		try {
			initSubscriber(app, publisherName);
			return true;
		}
		catch (SubscriberCreationException e) {
			// The publisher does not exist, so we are waiting for it.
		}

		// Wait for the publisher.
		String key = Publisher.KEY + "-" + publisherName;
		
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
			initSubscriber(app, publisherName);
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