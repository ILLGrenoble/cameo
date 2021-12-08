package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Application.Instance;
import fr.ill.ics.cameo.base.Application.This;
import fr.ill.ics.cameo.coms.impl.RequesterImpl;
import fr.ill.ics.cameo.coms.impl.ResponderImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;

/**
 * Class Requester.
 *
 */
public class Requester {

	private RequesterImpl impl;
	
	private Requester(RequesterImpl impl) {
		this.impl = impl;
	}
	
	private static RequesterImpl createRequester(Instance application, String name) throws RequesterCreationException {
		
		int responderId = application.getId();
		String responderPortName = ResponderImpl.RESPONDER_PREFIX + name;
		
		int requesterId = RequesterImpl.newRequesterId();
		String requesterPortName = RequesterImpl.getRequesterPortName(name, responderId, requesterId);
		
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
		
		return new RequesterImpl(application.getEndpoint(), requesterPort, responderPort, name, responderId, requesterId);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws RequesterCreationException, ConnectionTimeout
	 */
	static public Requester create(Instance application, String name) throws RequesterCreationException {
		return new Requester(createRequester(application, name));
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
		impl.terminate();
	}
		
	@Override
	public String toString() {
		return impl.toString();
	}
}