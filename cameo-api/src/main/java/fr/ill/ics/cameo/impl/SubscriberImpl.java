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

package fr.ill.ics.cameo.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.strings.Endpoint;

public class SubscriberImpl {
	
	private ServerImpl server; // server of instance
	private Zmq.Context context;
	private Endpoint serverEndpoint; // endpoint of server
	private int publisherPort;
	private int synchronizerPort;
	private Zmq.Socket subscriber;
	private String cancelEndpoint;
	private Zmq.Socket cancelPublisher;
	private String publisherName;
	private int numberOfSubscribers;
	private InstanceImpl instance;
	private boolean ended = false;
	private boolean canceled = false;
	private SubscriberWaitingImpl waiting = new SubscriberWaitingImpl(this);
	
	SubscriberImpl(ServerImpl server, Zmq.Context context, Endpoint serverEndpoint, int publisherPort, int synchronizerPort, String publisherName, int numberOfSubscribers, InstanceImpl instance) {
		this.server = server;
		this.context = context;
		this.serverEndpoint = serverEndpoint;
		this.publisherPort = publisherPort;
		this.synchronizerPort = synchronizerPort;
		this.publisherName = publisherName;
		this.numberOfSubscribers = numberOfSubscribers;
		this.instance = instance;
		
		waiting.add();
	}
	
	void init() throws ConnectionTimeout {
		
		// Create the subscriber
		subscriber = context.createSocket(Zmq.SUB);
		
		subscriber.connect(serverEndpoint.withPort(publisherPort).toString());
		subscriber.subscribe(Message.Event.SYNC);
		subscriber.subscribe(Message.Event.STREAM);
		subscriber.subscribe(Message.Event.ENDSTREAM);
		
		// Create an endpoint that should be unique
		cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		// Create a cancel publisher so that it sends the CANCEL message to the status subscriber (connected to 2 publishers)
		cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);

		// Subscribe to CANCEL
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Message.Event.CANCEL);
		
		// Subscribe to STATUS
		subscriber.connect(server.getStatusEndpoint().toString());
		subscriber.subscribe(Message.Event.STATUS);
		
		// Synchronize the subscriber only if the number of subscribers > 0
		if (numberOfSubscribers > 0) {
			
			// Create a socket that will be used for several requests.
			RequestSocket requestSocket = server.createRequestSocket(serverEndpoint.withPort(synchronizerPort).toString());
			
			// polling to wait for connection
			Zmq.Poller poller = context.createPoller(subscriber);
			
			boolean ready = false;
			while (!ready) {
				
				// The subscriber sends init messages to the publisher that returns SYNC message
				Zmq.Msg request = server.createSyncRequest();
				Zmq.Msg reply = null;
				try {
					reply = requestSocket.request(request);
					reply.destroy();
					request.destroy();

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
			Zmq.Msg request = server.createSubscribePublisherRequest();
			requestSocket.request(request);
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
			
			if (message.equals(Message.Event.STREAM)) {
				return subscriber.recv();
				
			} else if (message.equals(Message.Event.ENDSTREAM)) {
				ended = true;
				return null;
				
			} else if (message.equals(Message.Event.CANCEL)) {
				canceled = true;
				return null;
				
			} else if (message.equals(Message.Event.STATUS)) {
				byte[] statusMessage = subscriber.recv();
				
				try {
					// Get the JSON object.
					JSONObject status = server.parse(statusMessage);
					
					// Get the id.
					int id = JSON.getInt(status, Message.StatusEvent.ID);
										
					if (instance.getId() == id) {
						
						// Get the state.
						int state = JSON.getInt(status, Message.StatusEvent.APPLICATION_STATE);
						
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
				catch (ParseException e) {
					throw new UnexpectedException("Cannot parse response");
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
			
			if (message.equals(Message.Event.STREAM)) {
				byte[][] result = new byte[2][];
				result[0] = subscriber.recv();
				result[1] = subscriber.recv();
				
				return result;
				
			} else if (message.equals(Message.Event.ENDSTREAM)) {
				ended = true;
				return null;
				
			} else if (message.equals(Message.Event.CANCEL)) {
				canceled = true;
				return null;
				
			} else if (message.equals(Message.Event.STATUS)) {
				byte[] statusMessage = subscriber.recv();
				
				try {
					// Get the JSON request object.
					JSONObject request = server.parse(statusMessage);
					
					// Get the id.
					int id = JSON.getInt(request, Message.StatusEvent.ID);
					
					if (instance.getId() == id) {
						
						// Get the state.
						int state = JSON.getInt(request, Message.StatusEvent.APPLICATION_STATE);
						
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
				catch (ParseException e) {
					throw new UnexpectedException("Cannot parse response");
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
		
		return Message.parseString(data);
	}
	
	public void cancel() {
	
		cancelPublisher.sendMore(Message.Event.CANCEL);
		cancelPublisher.send(Message.Event.CANCEL);
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