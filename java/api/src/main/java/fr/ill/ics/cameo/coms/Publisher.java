package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.impl.PublisherImpl;
import fr.ill.ics.cameo.coms.impl.zmq.PublisherZmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;

/**
 * Class Publisher.
 *
 */
public class Publisher {

	private String name;
	private int numberOfSubscribers;
	private PublisherImpl impl;
	private PublisherWaiting waiting = new PublisherWaiting(this);
	private String key;
	
	public static String KEY = "publisher-55845880-56e9-4ad6-bea1-e84395c90b32";
	public static String PUBLISHER_PORT = "publisher_port";
	public static String SYNCHRONIZER_PORT = "synchronizer_port";
	public static String NUMBER_OF_SUBSCRIBERS = "n_subscribers";
	
	private Publisher(String name, int numberOfSubscribers) {
		
		this.name = name;
		this.numberOfSubscribers = numberOfSubscribers;
		
		//TODO Replace with factory.
		this.impl = new PublisherZmq(name, numberOfSubscribers);
		
		waiting.add();
	}
	
	private void init(String name) throws PublisherCreationException {
		
		// Init the publisher and synchronizer sockets.
		impl.init();
		
		// Store the publisher data.
		JSONObject publisherData = new JSONObject();
		publisherData.put(PUBLISHER_PORT, impl.getPublisherPort());
		publisherData.put(SYNCHRONIZER_PORT, impl.getSynchronizerPort());
		publisherData.put(NUMBER_OF_SUBSCRIBERS, numberOfSubscribers);
		
		key = KEY + "-" + name;
		
		This.getCom().storeKeyValue(key, publisherData.toJSONString());
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws PublisherCreationException, ConnectionTimeout
	 */
	static public Publisher create(String name, int numberOfSubscribers) throws PublisherCreationException {
		
		Publisher publisher = new Publisher(name, numberOfSubscribers);
		publisher.init(name);
		
		return publisher;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws PublisherCreationException, ConnectionTimeout
	 */
	static public Publisher create(String name) throws PublisherCreationException {
		return create(name, 0);
	}
	
	public String getName() {
		return name;
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
		
		try {
			This.getCom().removeKey(key);
		}
		catch (UndefinedKeyException e) {
			// No need to treat.
		}
		
		waiting.remove();
		impl.terminate();
	}
		
	@Override
	public String toString() {
		return "pub." + getName() + ":" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
}
