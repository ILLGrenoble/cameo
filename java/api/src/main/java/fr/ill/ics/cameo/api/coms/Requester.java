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

package fr.ill.ics.cameo.api.coms;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.api.base.App;
import fr.ill.ics.cameo.api.base.ConnectionTimeout;
import fr.ill.ics.cameo.api.base.ICancelable;
import fr.ill.ics.cameo.api.base.ITimeoutable;
import fr.ill.ics.cameo.api.base.InitException;
import fr.ill.ics.cameo.api.base.State;
import fr.ill.ics.cameo.api.base.StateObject;
import fr.ill.ics.cameo.api.base.SynchronizationTimeout;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.base.Timeout;
import fr.ill.ics.cameo.api.base.TimeoutCounter;
import fr.ill.ics.cameo.api.base.App.Com.KeyValueGetter;
import fr.ill.ics.cameo.api.coms.basic.Responder;
import fr.ill.ics.cameo.api.coms.impl.RequesterImpl;
import fr.ill.ics.cameo.api.factory.ImplFactory;
import fr.ill.ics.cameo.common.messages.JSON;
import fr.ill.ics.cameo.common.strings.AppIdentity;
import fr.ill.ics.cameo.common.strings.Endpoint;
import fr.ill.ics.cameo.common.strings.ServerIdentity;
import fr.ill.ics.cameo.common.strings.StringId;

/**
 * Class defining a requester. The request and response must be sent and received sequentially.
 */
public class Requester extends StateObject implements ITimeoutable, ICancelable {

	static class Checker {
		
		private Requester requester;
		private Thread thread = null;
		
		Checker(Requester requester) {
			this.requester = requester;
		}

		void start() {
			// Start the thread.
			thread = new Thread(new Runnable() {
				public void run() {
				
					// Wait for the app that can be canceled.
					int state = requester.app.waitFor();
					if (state == State.FAILURE) {
						// Cancel the requester if the app fails.
						requester.cancel();
					}
				}
			});
			
			thread.start();
		}

		void terminate() {
			// Cancel the wait for call.
			requester.app.cancel();

			// Clean the thread.
			if (thread != null) {
				try {
					thread.join();
				}
				catch (InterruptedException e) {
				}
			}
		}
	}
	
	
	private App app;
	private String responderName;
	private int timeout = -1;
	private boolean useProxy = false;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private RequesterImpl impl;
	private RequesterWaiting waiting = new RequesterWaiting(this);
	private String key;
	private KeyValueGetter keyValueGetter;
	private Checker checker;
	
	private Requester(App app, String responderName, boolean checkApp) {
		
		this.app = app;
		this.responderName = responderName;
		this.appName = app.getName();
		this.appId = app.getId();
		this.appEndpoint = app.getEndpoint();
		this.key = Responder.KEY + "-" + responderName;
		this.useProxy = app.usesProxy();
		this.impl = ImplFactory.createRequester();
		waiting.add();
		this.keyValueGetter = app.getCom().createKeyValueGetter(key);
		
		if (checkApp) {
			checker = new Checker(this);
		}
	}

	/**
	 * Returns a new requester.
	 * @param app The application where the responder is defined.
	 * @param responderName The responder name.
	 * @param checkApp If true, a thread is checking the state of the app and cancels the requester if it fails.
	 * @return A new Requester object.
	 */
	static public Requester create(App app, String responderName, boolean checkApp) {
		return new Requester(app, responderName, checkApp);
	}
	
	/**
	 * Returns a new requester.
	 * @param app The application where the responder is defined.
	 * @param responderName The responder name.
	 * @return A new Requester object.
	 */
	static public Requester create(App app, String responderName) {
		return new Requester(app, responderName, false);
	}

	/**
	 * Initializes the requester.
	 * @throws InitException if the requester cannot be created.
	 */
	@Override
	public void init() throws InitException {
		
		if (isReady()) {
			// The object is already initialized.
			return;
		}
		
		// Get the responder data.
		try {
			TimeoutCounter timeoutCounter = new TimeoutCounter(timeout);
			
			String jsonString = keyValueGetter.get(timeoutCounter);
			
			if (keyValueGetter.isCanceled()) {
				return;
			}
			
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
			
			impl.init(endpoint, StringId.from(key, appId), timeoutCounter);
		}
		catch (ConnectionTimeout e) {
			throw e;
		}
		catch (Timeout e) {
			throw new SynchronizationTimeout("Requester cannot synchronize responder '" + responderName + "'");
		}
		catch (Exception e) {
			throw new InitException("Cannot initialize requester to responder '" + responderName + "': " + e.getMessage());
		}
		
		if (checker != null) {
			checker.start();
		}
		
		setReady();
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
		keyValueGetter.cancel();
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
		
		if (checker != null) {
			checker.terminate();
			checker = null;
		}

		impl.terminate();
		setTerminated();
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