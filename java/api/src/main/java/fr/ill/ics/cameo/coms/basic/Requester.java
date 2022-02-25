package fr.ill.ics.cameo.coms.basic;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.KeyValue;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.UndefinedApplicationException;
import fr.ill.ics.cameo.base.UndefinedKeyException;
import fr.ill.ics.cameo.coms.RequesterCreationException;
import fr.ill.ics.cameo.coms.basic.impl.RequesterImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.StringId;

/**
 * Class Requester.
 *
 */
public class Requester {

	private boolean useProxy = false;
	private String responderName;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private RequesterImpl impl;
	private RequesterWaiting waiting = new RequesterWaiting(this);
	private String key;
	
	private Requester() {
		this.impl = ImplFactory.createBasicRequester();
		waiting.add();
	}

	public void setPollingTime(int value) {
		impl.setPollingTime(value);
	}

	public void setTimeout(int value) {
		impl.setTimeout(value);
	}
	
	private void tryInit(Instance app) throws RequesterCreationException {
		
		// Memorize proxy.
		useProxy = app.usesProxy();
		
		// Get the responder data.
		try {
			String jsonString = app.getCom().getKeyValue(key);
			
			JSONObject jsonData = This.getCom().parse(jsonString);
			
			int responderPort = JSON.getInt(jsonData, Responder.PORT);
					
			Endpoint endpoint;
			
			// The endpoint depends on the use of the proxy.
			if (useProxy) {
				// useProxy is inherited, so the server endpoint is the proxy endpoint.
				endpoint = app.getEndpoint();
			}
			else {
				endpoint = app.getEndpoint().withPort(responderPort);
			}
			
			impl.init(endpoint, StringId.from(appId, key));
		}
		catch (UndefinedApplicationException | UndefinedKeyException e) {
			throw new RequesterCreationException("");
		}
	}
	
	private boolean init(Instance app, String responderName) throws RequesterCreationException {
		
		this.responderName = responderName;
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		this.key = Responder.KEY + "-" + responderName;
		
		// Try to create the requester.
		// If the responder does not exist, an exception is thrown.
		try {
			tryInit(app);
			return true;
		}
		catch (RequesterCreationException e) {
			// The responder does not exist, so we are waiting for it.
		}
		
		// Wait for the responder.
		int lastState = app.waitFor(new KeyValue(key));
		
		// The state cannot be terminal or it means that the application has terminated that is not planned.
		if (lastState == Application.State.SUCCESS 
			|| lastState == Application.State.STOPPED
			|| lastState == Application.State.KILLED					
			|| lastState == Application.State.ERROR) {
			return false;
		}
		
		// The requester can be created.
		try {
			tryInit(app);
			return true;
		}
		catch (RequesterCreationException e) {
			// That should not happen.
			System.err.println("the responder " + responderName + " does not exist but should");
		}
		
		return false;
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
	
	public boolean hasTimedout() {
		return impl.hasTimedout();
	}
	
	public void terminate() {
		waiting.remove();
		impl.terminate();
	}
	
	@Override
	public String toString() {
		return "req." + responderName + ":" + appName + "." + appId + "@" + appEndpoint;
	}
}