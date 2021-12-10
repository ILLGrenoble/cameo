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

package fr.ill.ics.cameo.coms.impl;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.Application;
import fr.ill.ics.cameo.base.CancelIdGenerator;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class SubscriberImpl {
	
	private int publisherPort;
	private int synchronizerPort;
	private Zmq.Context context;
	private Zmq.Socket subscriber;
	private String cancelEndpoint;
	private Zmq.Socket cancelPublisher;
	private String publisherName;
	private int numberOfSubscribers;
	private Instance instance;
	private boolean ended = false;
	private boolean canceled = false;
	private SubscriberWaitingImpl waiting = new SubscriberWaitingImpl(this);
	
	public SubscriberImpl(int publisherPort, int synchronizerPort, String publisherName, int numberOfSubscribers, Instance instance) {
		this.publisherPort = publisherPort;
		this.synchronizerPort = synchronizerPort;
		this.publisherName = publisherName;
		this.numberOfSubscribers = numberOfSubscribers;
		this.instance = instance;
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		
		waiting.add();
	}
	
	public void init() throws ConnectionTimeout {
		
		// Create the subscriber
		subscriber = context.createSocket(Zmq.SUB);
		
		subscriber.connect(instance.getEndpoint().withPort(publisherPort).toString());
		subscriber.subscribe(Messages.Event.SYNC);
		subscriber.subscribe(Messages.Event.STREAM);
		subscriber.subscribe(Messages.Event.ENDSTREAM);
		
		// Create an endpoint that should be unique
		cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		// Create a cancel publisher so that it sends the CANCEL message to the status subscriber (connected to 2 publishers)
		cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);

		// Subscribe to CANCEL
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Messages.Event.CANCEL);
		
		// Subscribe to STATUS
		subscriber.connect(instance.getStatusEndpoint().toString());
		subscriber.subscribe(Messages.Event.STATUS);
		
		// Synchronize the subscriber only if the number of subscribers > 0
		if (numberOfSubscribers > 0) {
			
			// Create a socket that will be used for several requests.
			RequestSocket requestSocket = This.getCom().createRequestSocket(instance.getEndpoint().withPort(synchronizerPort).toString());
			
			// polling to wait for connection
			Zmq.Poller poller = context.createPoller(subscriber);
			
			boolean ready = false;
			while (!ready) {
				
				// The subscriber sends init messages to the publisher that returns SYNC message
				try {
					requestSocket.requestJSON(Messages.createSyncRequest());

				} catch (ConnectionTimeout e) {
					// do nothing
				}

				// return at the first response.
				if (poller.poll(100)) {
					ready = true;
				}
			}
			
			// The subscriber is connected and ready to receive data.
			// Notify the publisher that it can send data.
			JSONObject request = Messages.createSubscribePublisherRequest();
			requestSocket.requestJSON(request);
			requestSocket.terminate();
		}
	}
	
	public String getPublisherName() { 
		return publisherName;
	}
	
	public String getInstanceName() {
		return instance.getName();
	}
	
	public int getInstanceId() {
		return instance.getId();
	}
	
	public Endpoint getInstanceEndpoint() {
		return instance.getEndpoint();
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
			
			if (message.equals(Messages.Event.STREAM)) {
				return subscriber.recv();
				
			} else if (message.equals(Messages.Event.ENDSTREAM)) {
				ended = true;
				return null;
				
			} else if (message.equals(Messages.Event.CANCEL)) {
				canceled = true;
				return null;
				
			} else if (message.equals(Messages.Event.STATUS)) {
				byte[] statusMessage = subscriber.recv();
				
				// Get the JSON object.
				JSONObject status = This.getCom().parse(statusMessage);
				
				// Get the id.
				int id = JSON.getInt(status, Messages.StatusEvent.ID);
									
				if (instance.getId() == id) {
					
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
			
			if (message.equals(Messages.Event.STREAM)) {
				byte[][] result = new byte[2][];
				result[0] = subscriber.recv();
				result[1] = subscriber.recv();
				
				return result;
				
			} else if (message.equals(Messages.Event.ENDSTREAM)) {
				ended = true;
				return null;
				
			} else if (message.equals(Messages.Event.CANCEL)) {
				canceled = true;
				return null;
				
			} else if (message.equals(Messages.Event.STATUS)) {
				byte[] statusMessage = subscriber.recv();
				
				// Get the JSON request object.
				JSONObject request =  This.getCom().parse(statusMessage);
				
				// Get the id.
				int id = JSON.getInt(request, Messages.StatusEvent.ID);
				
				if (instance.getId() == id) {
					
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
		
		waiting.remove();
		
		context.destroySocket(subscriber);
		context.destroySocket(cancelPublisher);
	}

	@Override
	public String toString() {
		return "sub." + publisherName + ":" + instance.getName() + "." + instance.getId() + "@" + instance.getEndpoint();
	}
	
}