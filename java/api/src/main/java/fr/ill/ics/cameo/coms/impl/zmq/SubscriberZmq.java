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

package fr.ill.ics.cameo.coms.impl.zmq;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.CancelIdGenerator;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.coms.impl.SubscriberImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class SubscriberZmq implements SubscriberImpl {
	
	private Zmq.Context context;
	private Zmq.Socket subscriber;
	private String publisherIdentity;
	private String cancelEndpoint;
	private Zmq.Socket cancelPublisher;
	private int appId;
	private boolean ended = false;
	private boolean canceled = false;
	
	public void init(int appId, Endpoint endpoint, Endpoint appStatusEndpoint, String publisherIdentity) {

		this.appId = appId;
		this.publisherIdentity = publisherIdentity;
		
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		
		// Create the subscriber
		subscriber = context.createSocket(Zmq.SUB);
		subscriber.connect(endpoint.toString());
		
		// Subcribe to the publisher.
		subscriber.subscribe(publisherIdentity);
		
		// Create an endpoint that should be unique
		cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		// Create a cancel publisher so that it sends the CANCEL message to the status subscriber (connected to 2 publishers)
		cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);

		// Subscribe to CANCEL
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Messages.Event.CANCEL);
		
		// Subscribe to STATUS
		subscriber.connect(appStatusEndpoint.toString());
		subscriber.subscribe(Messages.Event.STATUS);
	}
	
	public boolean isEnded() {
		return ended;
	}
	
	public boolean isCanceled() {
		return canceled;
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
				else if (type == Messages.STREAM_END) {
					ended = true;
					return null;	
				}
			}
			else if (message.equals(Messages.Event.CANCEL)) {
				canceled = true;
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
					if (state == Application.State.SUCCESS 
							|| state == Application.State.STOPPED
							|| state == Application.State.KILLED
							|| state == Application.State.ERROR) {
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
					ended = true;
					return null;	
				}
			}
			else if (message.equals(Messages.Event.CANCEL)) {
				canceled = true;
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
					if (state == Application.State.SUCCESS 
							|| state == Application.State.STOPPED
							|| state == Application.State.KILLED
							|| state == Application.State.ERROR) {
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
	
		cancelPublisher.sendMore(Messages.Event.CANCEL);
		cancelPublisher.send(Messages.Event.CANCEL);
	}
	
	public void terminate() {
		context.destroySocket(subscriber);
		context.destroySocket(cancelPublisher);
	}
	
}