/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.App.Com.KeyValueGetter;
import eu.ill.cameo.api.base.ConnectionTimeout;
import eu.ill.cameo.api.base.ICancelable;
import eu.ill.cameo.api.base.ITimeoutable;
import eu.ill.cameo.api.base.InitException;
import eu.ill.cameo.api.base.PingableObject;
import eu.ill.cameo.api.base.State;
import eu.ill.cameo.api.base.SynchronizationTimeout;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.Timeout;
import eu.ill.cameo.api.base.TimeoutCounter;
import eu.ill.cameo.api.coms.basic.Responder;
import eu.ill.cameo.api.coms.impl.RequesterImpl;
import eu.ill.cameo.api.factory.ImplFactory;
import eu.ill.cameo.common.messages.JSON;
import eu.ill.cameo.common.strings.AppIdentity;
import eu.ill.cameo.common.strings.Endpoint;
import eu.ill.cameo.common.strings.ServerIdentity;
import eu.ill.cameo.common.strings.StringId;

/**
 * Class defining a requester. The request and response must be sent and received sequentially.
 */
public class Requester extends PingableObject implements ITimeoutable, ICancelable {

	static class Checker {
		
		private Requester requester;
		private App app;
		private Thread thread = null;
		
		Checker(Requester requester) {
			this.requester = requester;
			
			// Connect the app to have an instance that is accessed only by the Checker thread.
			this.app = requester.app.connect();
		}

		void start() {
			// Start the thread.
			thread = new Thread(new Runnable() {
				public void run() {
				
					// Wait for the app that can be canceled.
					int state = app.waitFor();
					if (state == State.FAILURE) {
						// Cancel the requester if the app fails.
						requester.cancel();
					}
				}
			});
			
			thread.start();
		}

		void terminate() {
			// Cancel the waitFor() call.
			app.cancel();

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
	private boolean checkApp = false;
	private int timeout = -1;
	private boolean useProxy = false;
	private String appName;
	private int appId;
	private Endpoint appEndpoint;
	private final Lock lock = new ReentrantLock();
	private RequesterImpl impl;
	private RequesterWaiting waiting = new RequesterWaiting(this);
	private String key;
	private KeyValueGetter keyValueGetter;
	private Checker checker;
	
	private Requester(App app, String responderName) {
		
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
	 * A thread is checking the state of the app and cancels the requester if it fails.
	 * @param value True if the app is checked.
	 */
	public void setCheckApp(boolean value) {
		checkApp = value;
	}
	
	/**
	 * Sets the timeout.
	 * @param value The value.
	 */
	@Override
	public void setTimeout(int value) {
		
		lock.lock();
		try {
			timeout = value;
			impl.setTimeout(value);
		}
		finally {
			lock.unlock();
		}
	}

	/**
	 * Sets the polling time.
	 * @param value The value.
	 */
	public void setPollingTime(int value) {
		impl.setPollingTime(value);
	}
	
	/**
	 * Initializes the requester.
	 * @throws InitException if the requester cannot be created.
	 * @throws SynchronizationTimeout if the requester cannot synchronize the responder.
	 */
	@Override
	public void init() throws InitException {
		
		if (isReady() || isCanceled()) {
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
		
		if (checkApp) {
			// The creation of the Checker object can throw a ConnectionTimeout exception.
			checker = new Checker(this);
			checker.start();
		}
		
		setReady();
		
		// Registers this pingable object.
		super.init();		
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
	 * If the requester timed out in the last request, then it is reinitialized and can time out during the synchronization. 
	 * @param request The binary request.
	 */
	public void send(byte[] request) {
		impl.send(request);
	}
	
	/**
	 * Sends a string request in one part.
	 * If the requester timed out in the last request, then it is reinitialized and can time out during the synchronization.
	 * @param request The string request.
	 */
	public void sendString(String request) {
		impl.send(request);
	}
	
	/**
	 * Sends a request in two binary parts.
	 * @param request1 The first part of the request.
	 * @param request2 The seconds part of the request.
	 * If the requester timed out in the last request, then it is reinitialized and can time out during the synchronization.
	 */
	public void sendTwoParts(byte[] request1, byte[] request2) {
		impl.sendTwoParts(request1, request2);
	}
	
	/**
	 * Starts a request. Should only be used with send() or sendTwoParts() and multiple receive().
	 * A call to endRequest() must be done after the multiple receive().
	 */
	public void startRequest() {
		lock.lock();
	}

	/**
	 * Ends a request with one send() or sendTwoParts() and multiple receive().
	 * Must be called after startRequest().
	 */
	public void endRequest() {
		lock.unlock();
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
	 * Requests a binary request in one part.
	 * If the requester timed out in the last request, then it is reinitialized and can time out during the synchronization. 
	 * @param request The binary request.
	 * @return The response or null.
	 */
	public byte[] request(byte[] request) {
		
		lock.lock();
		try {
			impl.send(request);
			return impl.receive();
		}
		finally {
			lock.unlock();
		}
	}
	
	/**
	 * Requests a string request in one part.
	 * If the requester timed out in the last request, then it is reinitialized and can time out during the synchronization.
	 * @param request The string request.
	 * @return The response or null.
	 */
	public String request(String request) {
		
		lock.lock();
		try {
			impl.send(request);
			return impl.receiveString();
		}
		finally {
			lock.unlock();
		}
	}
	
	/**
	 * Sends a request in two binary parts.
	 * If the requester timed out in the last request, then it is reinitialized and can time out during the synchronization.
	 * @param requestPart1 The first part of the request.
	 * @param requestPart2 The seconds part of the request.
	 * @return The response or null. Use Messages.parseString() to convert the response to a string.
	 */
	public byte[] request(byte[] requestPart1, byte[] requestPart2) {
		
		lock.lock();
		try {
			impl.sendTwoParts(requestPart1, requestPart2);
			return impl.receive();
		}
		finally {
			lock.unlock();
		}
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
	
	@Override
	public boolean ping(int timeout) {

		if (!isReady()) {
			return false; 
		}
		
		// Lock only if the mutex is available.
		if (lock.tryLock()) {
			try {
				int pingTimeout = 0;
				if (this.timeout == -1) {
					pingTimeout = timeout * 1000;
					impl.setTimeout(pingTimeout);
				}
				
				impl.ping();
				byte[] response = impl.receive();
				
				if (timeout == -1) {
					impl.setTimeout(0);
				}
				
				if (response != null) {
					return true;
				}
				
				return false;
			}
			finally {
				lock.unlock();
			}
		}

		return false;
	}
	
	/**
	 * Terminates the communication.
	 */
	@Override
	public void terminate() {
		
		// Unregisters this pingable object.
		super.terminate();
		
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