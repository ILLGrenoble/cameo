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

package fr.ill.ics.cameo.api.coms.impl.zmq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.api.base.IdGenerator;
import fr.ill.ics.cameo.api.base.State;
import fr.ill.ics.cameo.api.base.This;
import fr.ill.ics.cameo.api.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.api.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.com.Zmq;
import fr.ill.ics.cameo.common.messages.JSON;
import fr.ill.ics.cameo.common.messages.Messages;
import fr.ill.ics.cameo.common.strings.Endpoint;

public class SubscriberZmq implements SubscriberImpl {
	
	private Zmq.Context context;
	private Zmq.Socket subscriber;
	private String publisherIdentity;
	private String cancelEndpoint;
	private Zmq.Socket cancelPublisher;
	private int appId;
	private AtomicBoolean ended = new AtomicBoolean(false);
	private AtomicBoolean canceled = new AtomicBoolean(false);
	private Zmq.Poller poller;
	
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
	
	/**
	 * 
	 * @return the byte[] data. If the return value is null, then the stream is finished. 
	 */
	public byte[] receive() {

		while (true) {
			String message = subscriber.recvStr();
			
			if (message.equals(publisherIdentity)) {
				
				String messageTypePart = subscriber.recvStr();
				
				// Get the JSON object.
				JSONObject messageType = This.getCom().parse(messageTypePart);
				
				// Get the type.
				long type = JSON.getLong(messageType, Messages.TYPE);
				
				if (type == Messages.STREAM) {
					return subscriber.recv();
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
				byte[] statusMessage = subscriber.recv();
				
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
			String message = subscriber.recvStr();
			
			if (message.equals(publisherIdentity)) {
				
				String messageTypePart = subscriber.recvStr();
				
				// Get the JSON object.
				JSONObject messageType = This.getCom().parse(messageTypePart);
				
				// Get the type.
				long type = JSON.getLong(messageType, Messages.TYPE);
				
				if (type == Messages.STREAM) {
					byte[][] result = new byte[2][];
					result[0] = subscriber.recv();
					result[1] = subscriber.recv();
					
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
				byte[] statusMessage = subscriber.recv();
				
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