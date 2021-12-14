package fr.ill.ics.cameo.coms;

import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.impl.RequesterImpl;
import fr.ill.ics.cameo.coms.impl.ResponderImpl;
import fr.ill.ics.cameo.coms.impl.zmq.RequesterZmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;

/**
 * Class Requester.
 *
 */
public class Requester {

	private String name;
	private int requesterId;
	private int responderId;
	private RequesterImpl impl;
	private RequesterWaiting waiting = new RequesterWaiting(this);
	private static AtomicInteger requesterCounter = new AtomicInteger();
	
	private Requester(String name) {
		this.name = name;
		this.impl = new RequesterZmq();
		waiting.add();
	}

	private static int newRequesterId() {
		return requesterCounter.incrementAndGet();
	}

	private static String getRequesterPortName(String name, int responderId, int requesterId) {
		return RequesterImpl.REQUESTER_PREFIX + name + "." + responderId + "." + requesterId;
	}
	
	private void init(Instance application, String name) throws RequesterCreationException {
		
		responderId = application.getId();
		String responderPortName = ResponderImpl.RESPONDER_PREFIX + name;
		
		requesterId = newRequesterId();
		String requesterPortName = getRequesterPortName(name, responderId, requesterId);
		
		// First connect to the responder.
		JSONObject request = Messages.createConnectPortV0Request(responderId, responderPortName);
		JSONObject response = application.getCom().requestJSON(request);
		
		int responderPort = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (responderPort == -1) {
			
			// Wait for the responder port.
			application.waitFor(responderPortName);

			// Retry to connect.
			request = Messages.createConnectPortV0Request(responderId, responderPortName);
			response = application.getCom().requestJSON(request);
			responderPort = JSON.getInt(response, Messages.RequestResponse.VALUE);
			
			if (responderPort == -1) {
				throw new RequesterCreationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
			}
		}
		
		// Request a requester port.
		request = Messages.createRequestPortV0Request(This.getId(), requesterPortName);
		
		response = This.getCom().requestJSON(request);
		int requesterPort = JSON.getInt(response, Messages.RequestResponse.VALUE);
		
		if (requesterPort == -1) {
			throw new RequesterCreationException(JSON.getString(response, Messages.RequestResponse.MESSAGE));
		}
		
		impl.init(application.getEndpoint(), requesterPort, responderPort, name);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws RequesterCreationException, ConnectionTimeout
	 */
	static public Requester create(Instance application, String name) throws RequesterCreationException {
		
		Requester requester = new Requester(name);
		requester.init(application, name);
		
		return requester;
	}
	
	public String getName() {
		return impl.getName();
	}
	
	public void send(byte[] request) {
		impl.send(request);
	}
	
	public void send(String request) {
		impl.send(request);
	}
	
	public void sendTwoParts(byte[] request1, byte[] request2) {
		impl.sendTwoParts(request1, request2);
	}
	
	public byte[] receive() {
		return impl.receive();
	}
	
	public String receiveString() {
		return impl.receiveString();
	}
	
	public void cancel() {
		impl.cancel();			
	}
	
	public boolean isCanceled() {
		return impl.isCanceled();
	}
	
	public void terminate() {
		waiting.remove();
		impl.terminate();

		try {
			This.getCom().removePort(getRequesterPortName(name, responderId, requesterId));
			
		} catch (Exception e) {
			System.err.println("Cannot terminate requester: " + e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return RequesterImpl.REQUESTER_PREFIX + name + "." + requesterId + ":" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
}