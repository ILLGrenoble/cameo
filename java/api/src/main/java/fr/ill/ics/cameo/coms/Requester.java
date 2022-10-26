/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.App.Com.KeyValueGetter;
import fr.ill.ics.cameo.base.ICancelable;
import fr.ill.ics.cameo.base.IObject;
import fr.ill.ics.cameo.base.ITimeoutable;
import fr.ill.ics.cameo.base.InitException;
import fr.ill.ics.cameo.base.KeyValueGetterException;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.coms.basic.Responder;
import fr.ill.ics.cameo.coms.impl.RequesterImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.strings.AppIdentity;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.ServerIdentity;
import fr.ill.ics.cameo.strings.StringId;

/**
 * Class defining a requester. The request and response must be sent and received sequentially.
 */
public class Requester implements IObject, ITimeoutable, ICancelable {

	private App app;
	private String responderName;
	private int timeout;
	private boolean useProxy = false;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private RequesterImpl impl;
	private RequesterWaiting waiting = new RequesterWaiting(this);
	private String key;
	
	private Requester(App app, String responderName) {
		
		this.app = app;
		this.responderName = responderName;
		
		this.impl = ImplFactory.createRequester();
		waiting.add();
	}

	/**
	 * Returns a new requester.
	 * @param app The application where the responder is defined.
	 * @param responderName The responder name.
	 * @return A new Requester object.
	 */
	static public Requester create(App app, String responderName) {
		return new Requester(app, responderName);
	}

	/**
	 * Initializes the requester.
	 * @throws InitException if the requester cannot be created.
	 */
	@Override
	public void init() throws InitException {
		
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		this.key = Responder.KEY + "-" + responderName;
		this.useProxy = app.usesProxy();
		
		// Get the responder data.
		try {
			KeyValueGetter getter = app.getCom().getKeyValueGetter(key);
			String jsonString = getter.get();
			JSONObject jsonData = This.getCom().parse(jsonString);
					
			Endpoint endpoint;
			
			// The endpoint depends on the use of the proxy.
			if (useProxy) {
				int responderPort = app.getCom().getResponderProxyPort();
				endpoint = app.getEndpoint().withPort(responderPort);
			}
			else {
				int responderPort = JSON.getInt(jsonData, Responder.PORT);
				endpoint = app.getEndpoint().withPort(responderPort);
			}
			
			impl.init(endpoint, StringId.from(key, appId));
		}
		catch (KeyValueGetterException e) {
			throw new InitException("Cannot initialize requester: " + e.getMessage());
		}
	}

	/**
	 * Sets the timeout.
	 * @param value The value.
	 */
	@Override
	public void setTimeout(int value) {
		timeout = value;
		impl.setTimeout(value);
	}
	
	/**
	 * Gets the timeout.
	 * @return The timeout.
	 */
	@Override
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Sets the polling time.
	 * @param value The value.
	 */
	public void setPollingTime(int value) {
		impl.setPollingTime(value);
	}
	
	/**
	 * Gets the responder name.
	 * @return The responder name.
	 */
	public String getResponderName() {
		return responderName;
	}
	
	/**
	 * Gets the application name.
	 * @return The application name.
	 */
	public String getAppName() {
		return appName;
	}
	
	/**
	 * Gets the application id.
	 * @return The application id.
	 */	
	public int getAppId() {
		return appId;
	}
	
	/**
	 * Gets the application endpoint.
	 * @return The application endpoint.
	 */
	public Endpoint getAppEndpoint() {
		return appEndpoint;
	}
	
	/**
	 * Sends a binary request in one part.
	 * @param request The binary request.
	 */
	public void send(byte[] request) {
		impl.send(request);
	}
	
	/**
	 * Sends a string request in one part.
	 * @param request The string request.
	 */
	public void sendString(String request) {
		impl.send(request);
	}
	
	/**
	 * Sends a request in two binary parts.
	 * @param request1 The first part of the request.
	 * @param request2 The seconds part of the request.
	 */
	public void sendTwoParts(byte[] request1, byte[] request2) {
		impl.sendTwoParts(request1, request2);
	}
	
	/**
	 * Returns a byte array or nothing if the requester is canceled or a timeout occurred.
	 * @return The response or null.
	 */
	public byte[] receive() {
		return impl.receive();
	}
	
	/**
	 * Returns a string or nothing if the requester is canceled or a timeout occurred.
	 * @return The response or null.
	 */
	public String receiveString() {
		return impl.receiveString();
	}
	
	/**
	 * Cancels the requester. Unblocks the receive() call in another thread.
	 */
	@Override
	public void cancel() {
		impl.cancel();			
	}
	
	/**
	 * Returns true if the requester has been canceled.
	 * @return True if the requester has been canceled.
	 */
	@Override
	public boolean isCanceled() {
		return impl.isCanceled();
	}
	
	/**
	 * Returns true if the requester has timed out.
	 * @return True if the requester has timed out.
	 */
	public boolean hasTimedout() {
		return impl.hasTimedout();
	}
	
	/**
	 * Terminates the communication.
	 */
	@Override
	public void terminate() {
		waiting.remove();
		impl.terminate();
	}
	
	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("type", "requester");
		result.put("name", responderName);
		result.put("app", new AppIdentity(appName, appId, new ServerIdentity(appEndpoint.toString(), useProxy)).toJSON());
		
		return result.toJSONString();
	}
}