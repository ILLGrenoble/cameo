package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.KeyAlreadyExistsException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.basic.Request;
import fr.ill.ics.cameo.coms.basic.Responder;
import fr.ill.ics.cameo.coms.impl.PublisherImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.StringId;

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
	private Responder responder = null;
	
	public static final String KEY = "publisher-55845880-56e9-4ad6-bea1-e84395c90b32";
	public static final String PUBLISHER_PORT = "publisher_port";
	public static final String NUMBER_OF_SUBSCRIBERS = "n_subscribers";
	public static final String RESPONDER_PREFIX = "publisher:";
	public static final long SUBSCRIBE_PUBLISHER = 100;
	
	private Publisher(String name, int numberOfSubscribers) {
		
		this.name = name;
		this.numberOfSubscribers = numberOfSubscribers;
		
		this.impl = ImplFactory.createPublisher();
		
		waiting.add();
	}
	
	private void init(String name) throws PublisherCreationException {
		
		// Set the key.
		key = KEY + "-" + name;
		
		// Init the publisher and synchronizer sockets.
		impl.init(StringId.from(This.getId(), key));
		
		// Store the publisher data.
		JSONObject publisherData = new JSONObject();
		publisherData.put(PUBLISHER_PORT, impl.getPublisherPort());
		publisherData.put(NUMBER_OF_SUBSCRIBERS, numberOfSubscribers);
		
		try {
			This.getCom().storeKeyValue(key, publisherData.toJSONString());
		}
		catch (KeyAlreadyExistsException e) {
			throw new PublisherCreationException("A publisher with the name \"" + name + "\" already exists");
		}
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
		
		System.out.println("Wait for subscribers");
		
		try {
			// Create the responder.
			responder = Responder.create(RESPONDER_PREFIX + name);
		
			// Loop until the number of subscribers is reached.
			int counter = 0;
			
			while (counter < numberOfSubscribers) {
				
				Request request = responder.receive();
		
				System.out.println("Received request");
				
				if (request == null) {
					return false;
				}
				
				System.out.println("Received request " + request.get());
				
				// Get the JSON request object.
				JSONObject jsonRequest = This.getCom().parse(request.get());
				
				// Get the type.
				long type = JSON.getLong(jsonRequest, Messages.TYPE);
				
				if (type == SUBSCRIBE_PUBLISHER) {
					counter++;
				}
				
				request.reply("OK");
			}
			
			return !responder.isCanceled();
		}
		catch (ResponderCreationException e) {
			System.err.println("Error, cannot create responder");
			return false;
		}
		finally {
			// Destroy responder.
			if (responder != null) {
				responder.terminate();
				responder = null;
			}
		}
	}
	
	/**
	 * Cancels the wait for subscribers.
	 */
	public void cancelWaitForSubscribers() {
		if (responder != null) {
			responder.cancel();
		}
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
