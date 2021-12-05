package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Application.This;
import fr.ill.ics.cameo.coms.impl.PublisherImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;

/**
 * Class Publisher.
 *
 */
public class Publisher {

	private PublisherImpl impl;
	
	private Publisher(PublisherImpl impl) {
		this.impl = impl;
	}
	
	static PublisherImpl createPublisher(String name, int numberOfSubscribers) throws PublisherCreationException {
	
		JSONObject request = Messages.createCreatePublisherRequest(This.getId(), name, numberOfSubscribers);
		JSONObject response = This.getCom().request(request);
	
		int publisherPort = JSON.getInt(response, Messages.PublisherResponse.PUBLISHER_PORT);
		if (publisherPort == -1) {
			throw new PublisherCreationException(JSON.getString(response, Messages.PublisherResponse.MESSAGE));
		}
		int synchronizerPort = JSON.getInt(response, Messages.PublisherResponse.SYNCHRONIZER_PORT);
		
		return new PublisherImpl(publisherPort, synchronizerPort, name, numberOfSubscribers);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws PublisherCreationException, ConnectionTimeout
	 */
	static public Publisher create(String name, int numberOfSubscribers) throws PublisherCreationException {
		return new Publisher(createPublisher(name, numberOfSubscribers));
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws PublisherCreationException, ConnectionTimeout
	 */
	static public Publisher create(String name) throws PublisherCreationException {
		return new Publisher(createPublisher(name, 0));
	}
	
	public String getName() {
		return impl.getName();
	}
	
	/**
	 * Returns true if the wait succeeds or false if it was canceled.
	 * @return
	 */
	public boolean waitForSubscribers() {
		return impl.waitForSubscribers();
	}
	
	/**
	 * Cancels the wait for subscribers.
	 */
	public void cancelWaitForSubscribers() {
		impl.cancelWaitForSubscribers();
	}

	public void send(byte[] data) {
		impl.send(data);
	}
	
	public void send(String data) {
		impl.send(data);
	}
			
	public void sendTwoParts(byte[] data1, byte[] data2) {
		impl.sendTwoParts(data1, data2);
	}
	
	public void sendEnd() {
		impl.sendEnd();
	}
	
	public boolean isEnded() {
		return impl.isEnded();
	}
	
	public void terminate() {
		impl.terminate();
	}
		
	@Override
	public String toString() {
		return impl.toString();
	}
}
