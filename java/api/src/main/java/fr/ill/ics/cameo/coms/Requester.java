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
import fr.ill.ics.cameo.strings.Endpoint;

/**
 * Class Requester.
 *
 */
public class Requester {

	private String responderName;
	private int requesterId;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private RequesterImpl impl;
	private RequesterWaiting waiting = new RequesterWaiting(this);
	private static AtomicInteger requesterCounter = new AtomicInteger();
	
	private Requester() {
		this.impl = new RequesterZmq();
		waiting.add();
	}

	private static int newRequesterId() {
		return requesterCounter.incrementAndGet();
	}

	private static String getRequesterPortName(String responderName, int responderId, int requesterId) {
		return RequesterImpl.REQUESTER_PREFIX + responderName + "." + responderId + "." + requesterId;
	}
	
	private void init(Instance app, String responderName) throws RequesterCreationException {
		
		this.responderName = responderName;
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		
		String responderPortName = ResponderImpl.RESPONDER_PREFIX + responderName;
		
		requesterId = newRequesterId();
		String requesterPortName = getRequesterPortName(responderName, appId, requesterId);
		
		// First connect to the responder.
		JSONObject request = Messages.createConnectPortV0Request(appId, responderPortName);
		JSONObject response = app.getCom().requestJSON(request);
		
		int responderPort = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (responderPort == -1) {
			
			// Wait for the responder port.
			app.waitFor(responderPortName);

			// Retry to connect.
			request = Messages.createConnectPortV0Request(appId, responderPortName);
			response = app.getCom().requestJSON(request);
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
		
		impl.init(app.getEndpoint(), requesterPort, responderPort);
	}
	
	/**
	 * 
	 * @param responderName
	 * @return
	 * @throws RequesterCreationException, ConnectionTimeout
	 */
	static public Requester create(Instance app, String responderName) throws RequesterCreationException {
		
		Requester requester = new Requester();
		requester.init(app, responderName);
		
		return requester;
	}
	
	public String getResponderName() {
		return responderName;
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
			This.getCom().removePort(getRequesterPortName(responderName, appId, requesterId));
			
		} catch (Exception e) {
			System.err.println("Cannot terminate requester: " + e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return RequesterImpl.REQUESTER_PREFIX + responderName + ":" + appName + "." + appId + "@" + appEndpoint;
	}
}