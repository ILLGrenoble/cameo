package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.Application.Instance;
import fr.ill.ics.cameo.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

/**
 * Class Subscriber. 
 *
 */
public class Subscriber {
	
	private SubscriberImpl impl;
	
	private Subscriber(SubscriberImpl impl) {
		this.impl = impl;
	}
	
	private static SubscriberImpl createSubscriber(String publisherName, Instance instance) throws SubscriberCreationException {
		
		JSONObject request = Messages.createConnectPublisherRequest(instance.getId(), publisherName);
		JSONObject response = instance.getCom().requestJSON(request);
		
		int publisherPort = JSON.getInt(response, Messages.PublisherResponse.PUBLISHER_PORT);
		
		if (publisherPort == -1) {
			throw new SubscriberCreationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		
		int synchronizerPort = JSON.getInt(response, Messages.PublisherResponse.SYNCHRONIZER_PORT);
		int numberOfSubscribers = JSON.getInt(response, Messages.PublisherResponse.NUMBER_OF_SUBSCRIBERS);
		
		SubscriberImpl subscriber = new SubscriberImpl(publisherPort, synchronizerPort, publisherName, numberOfSubscribers, instance);
		subscriber.init();
		
		return subscriber;
	}
	
	static SubscriberImpl createSubscriber(Instance application, String publisherName) {
		
		try {
			SubscriberImpl subscriber = createSubscriber(publisherName, application);
			return subscriber;
			
		} catch (SubscriberCreationException e) {
			// the publisher does not exist, so we are waiting for it
		}
		
		// waiting for the publisher
		int lastState = application.waitFor(publisherName);
		
		// state cannot be terminal or it means that the application has terminated that is not planned.
		if (lastState == Application.State.SUCCESS 
			|| lastState == Application.State.STOPPED
			|| lastState == Application.State.KILLED					
			|| lastState == Application.State.ERROR) {
			return null;
		}
		
		try {
			SubscriberImpl subscriber = createSubscriber(publisherName, application);
			return subscriber;
			
		} catch (SubscriberCreationException e) {
			// that should not happen
			System.err.println("the publisher " + publisherName + " does not exist but should");
		}
		
		return null;
	}
	
	/**
	 * Subscribes to the application publisher.
	 * @param publisherName
	 * @return
	 */
	public static Subscriber create(Instance application, String publisherName) {
		return new Subscriber(createSubscriber(application, publisherName));
	}
			
	public String getPublisherName() { 
		return impl.getPublisherName();
	}
	
	public String getInstanceName() {
		return impl.getInstanceName();
	}
	
	public int getInstanceId() {
		return impl.getInstanceId();
	}
	
	public Endpoint getInstanceEndpoint() {
		return impl.getInstanceEndpoint();
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
		impl.terminate();
	}

	@Override
	public String toString() {
		return impl.toString();
	}
}