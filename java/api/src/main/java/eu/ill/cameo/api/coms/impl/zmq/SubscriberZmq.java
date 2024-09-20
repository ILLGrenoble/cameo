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

package eu.ill.cameo.api.coms.impl.zmq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.IdGenerator;
import eu.ill.cameo.api.base.State;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.impl.zmq.ContextZmq;
import eu.ill.cameo.api.coms.impl.SubscriberImpl;
import eu.ill.cameo.com.Zmq;
import eu.ill.cameo.common.messages.JSON;
import eu.ill.cameo.common.messages.Messages;
import eu.ill.cameo.common.strings.Endpoint;

public class SubscriberZmq implements SubscriberImpl {
	
	private int pollingTime = 100;
	private int timeout = 0;
	
	private Zmq.Context context;
	private Zmq.Socket subscriber;
	private String publisherIdentity;
	private String cancelEndpoint;
	private Zmq.Socket cancelPublisher;
	private int appId;
	private AtomicBoolean ended = new AtomicBoolean(false);
	private AtomicBoolean canceled = new AtomicBoolean(false);
	private AtomicBoolean timedout = new AtomicBoolean(false);
	private Zmq.Poller poller;
	
	public void setPollingTime(int value) {
		pollingTime = value;
	}
	
	public void setTimeout(int value) {
		timeout = value;
	}
	
	public void init(int appId, Endpoint endpoint, Endpoint appStatusEndpoint, String publisherIdentity, boolean checkApp) {

		this.appId = appId;
		this.publisherIdentity = publisherIdentity;
		
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		
		// Create the subscriber
		subscriber = context.createSocket(Zmq.SUB);
		subscriber.connect(endpoint.toString());
		
		// Subcribe to the publisher.
		subscriber.subscribe(publisherIdentity);
		
		// Create an endpoint that should be unique
		cancelEndpoint = "inproc://" + IdGenerator.newStringId();
		
		// Create a cancel publisher so that it sends the CANCEL message to the status subscriber (connected to 2 publishers)
		cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);

		// Subscribe to CANCEL
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Messages.Event.CANCEL);
		
		// Subscribe to STATUS if the app is checked.
		if (checkApp) {
			subscriber.connect(appStatusEndpoint.toString());
			subscriber.subscribe(Messages.Event.STATUS);
		}
		
		// Create the poller.
		poller = this.context.createPoller(1);
		poller.register(subscriber);
	}
	
	public boolean sync(int timeout) {
		
		poller.poll(timeout);
		return poller.pollin(0);
	}
	
	public boolean hasEnded() {
		return ended.get();
	}
	
	public boolean isCanceled() {
		return canceled.get();
	}
	
	@Override
	public boolean hasTimedout() {
		return timedout.get();
	}
	
	private byte[] receiveMessage() {
		
		// Reset timeout.
		timedout.set(false);
		
		// Define the number of iterations.
		int n = 0;
		if (pollingTime > 0) {
			n = timeout / pollingTime + 1;
		}

		// Create the poller.
		Zmq.Poller poller = this.context.createPoller(1);
		poller.register(subscriber);
		
		// Infinite loop if timeout is 0 or finite loop if timeout is defined.
		int i = 0;
		while (i < n || timeout == 0) {

			// Check if the requester has been canceled.
			if (canceled.get()) {
				return null;
			}

			// Poll the requester.
			poller.poll(pollingTime);
			if (poller.pollin(0)) {
				//return Zmq.Msg.recvMsg(subscriber);
				return subscriber.recv();
			}

			i++;
		}

		// Timeout occurred.
		timedout.set(true);

		// Reset the socket because it cannot be reused after a timeout.
		//resetSocket();
		
		return null;
	}
	
	private String receiveStringMessage() {
		
		byte[] response = receiveMessage();
		
		if (response != null) {
			return Messages.parseString(response);
		}
		
		return null;
	}
	
	/**
	 * 
	 * @return the byte[] data. If the return value is null, then the stream is finished. 
	 */
	public byte[] receive() {

		while (true) {
			String message = receiveStringMessage();
			if (message == null) {
				return null;
			}
			
			if (message.equals(publisherIdentity)) {
				
				String messageTypePart = receiveStringMessage();
				if (messageTypePart == null) {
					return null;
				}
				
				// Get the JSON object.
				JSONObject messageType = This.getCom().parse(messageTypePart);
				
				// Get the type.
				long type = JSON.getLong(messageType, Messages.TYPE);
				
				if (type == Messages.STREAM) {
					return receiveMessage();
				}
				else if (type == Messages.SYNC_STREAM) {
					// Do nothing.
				}
				else if (type == Messages.STREAM_END) {
					ended.set(true);
					return null;	
				}
			}
			else if (message.equals(Messages.Event.CANCEL)) {
				return null;
			}
			else if (message.equals(Messages.Event.STATUS)) {
				byte[] statusMessage = receiveMessage();
				if (statusMessage == null) {
					return null;
				}
				
				// Get the JSON object.
				JSONObject status = This.getCom().parse(statusMessage);
				
				// Get the id.
				int id = JSON.getInt(status, Messages.StatusEvent.ID);
									
				if (appId == id) {
					
					// Get the state.
					int state = JSON.getInt(status, Messages.StatusEvent.APPLICATION_STATE);
					
					// Test if the state is terminal
					if (state == State.SUCCESS 
							|| state == State.STOPPED
							|| state == State.KILLED
							|| state == State.FAILURE) {
						// Exit because the remote application has terminated.
						return null;
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @return the byte[] data. If the return value is null, then the stream is finished. 
	 */
	public byte[][] receiveTwoParts() {

		while (true) {
			String message = receiveStringMessage();
			if (message == null) {
				return null;
			}
			
			if (message.equals(publisherIdentity)) {
				
				String messageTypePart = receiveStringMessage();
				if (messageTypePart == null) {
					return null;
				}
				
				// Get the JSON object.
				JSONObject messageType = This.getCom().parse(messageTypePart);
				
				// Get the type.
				long type = JSON.getLong(messageType, Messages.TYPE);
				
				if (type == Messages.STREAM) {
					byte[][] result = new byte[2][];
					result[0] = receiveMessage();
					result[1] = receiveMessage();
					
					return result;
				}
				else if (type == Messages.STREAM_END) {
					ended.set(true);
					return null;	
				}
			}
			else if (message.equals(Messages.Event.CANCEL)) {
				return null;
			}
			else if (message.equals(Messages.Event.STATUS)) {
				byte[] statusMessage = receiveMessage();
				if (statusMessage == null) {
					return null;
				}
				
				// Get the JSON request object.
				JSONObject request =  This.getCom().parse(statusMessage);
				
				// Get the id.
				int id = JSON.getInt(request, Messages.StatusEvent.ID);
				
				if (appId == id) {
					
					// Get the state.
					int state = JSON.getInt(request, Messages.StatusEvent.APPLICATION_STATE);
					
					// Test if the state is terminal
					if (state == State.SUCCESS 
							|| state == State.STOPPED
							|| state == State.KILLED
							|| state == State.FAILURE) {
						// Exit because the remote application has terminated.
						return null;
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @return the string data. If the return value is null, then the stream is finished. 
	 */
	public String receiveString() {
		
		byte[] data = receive();
		
		if (data == null) {
			return null;
		}
		
		return Messages.parseString(data);
	}
	
	public void cancel() {	
	
		canceled.set(true);
		
		if (cancelPublisher != null) {
			cancelPublisher.sendMore(Messages.Event.CANCEL);
			cancelPublisher.send(Messages.Event.CANCEL);
		}
	}
	
	public void terminate() {
		
		if (context != null) {
			context.destroySocket(subscriber);
			context.destroySocket(cancelPublisher);
		}
	}
	
}