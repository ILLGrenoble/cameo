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
import fr.ill.ics.cameo.base.Application.This;
import fr.ill.ics.cameo.base.impl.ContextImpl;
import fr.ill.ics.cameo.base.impl.RequestSocket;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class PublisherImpl {

	private int synchronizerPort;
	private String name;
	private int numberOfSubscribers;
	private Zmq.Context context;
	private Zmq.Socket publisher = null;
	private boolean ended = false;
	private PublisherWaitingImpl waiting = new PublisherWaitingImpl(this);
	
	public PublisherImpl(int publisherPort, int synchronizerPort, String name, int numberOfSubscribers) {
		this.synchronizerPort = synchronizerPort;
		this.name = name;
		this.numberOfSubscribers = numberOfSubscribers;
		this.context = ((ContextImpl)This.getCom().getContext()).getContext();
		
		// create a socket for publishing
		publisher = context.createSocket(Zmq.PUB);
		publisher.bind("tcp://*:" + publisherPort);
		
		waiting.add();
	}
	
	public String getName() {
		return name;
	}
		
	public boolean waitForSubscribers() {
				
		if (numberOfSubscribers <= 0) {
			return true;
		}
		
		Zmq.Socket synchronizer = null;
		boolean canceled = false;
		
		try {
			// create a socket to receive the messages from the subscribers
			synchronizer = context.createSocket(Zmq.REP);
			String endpoint = "tcp://*:" + synchronizerPort;
			
			synchronizer.bind(endpoint);
			
			// loop until the number of subscribers is reached
			int counter = 0;
			
			while (counter < numberOfSubscribers) {

				Zmq.Msg message = null;
				Zmq.Msg reply = null;
				
				try {
					message = Zmq.Msg.recvMsg(synchronizer);
					
					if (message == null) {
						break;
					}
							
					// Get the JSON request object.
					JSONObject request = This.getCom().parse(message.getFirstData());
					
					// Get the type.
					long type = JSON.getLong(request, Messages.TYPE);
					
					if (type == Messages.SYNC) {
						reply = processSyncRequest();						
					}
					else if (type == Messages.SUBSCRIBE_PUBLISHER_v0) {
						counter++;
						reply = processSubscribePublisherRequest();
					}
					else if (type == Messages.CANCEL) {
						canceled = true;
						counter = numberOfSubscribers;
						message.send(synchronizer);
					}
					else {
						System.err.println("Unknown message type " + type);
						message.send(synchronizer);
					}
					
					// send to the client
					if (reply != null) {
						reply.send(synchronizer);
					}
				}
				finally {
					
					if (message != null) {
						message.destroy();
					}	
					
					if (reply != null) {
						reply.destroy();
					}
				}
			}
			
		} finally {
			// destroy synchronizer socket as we do not need it anymore.
			if (synchronizer != null) {
				context.destroySocket(synchronizer);
			}	
		}
		
		return !canceled;
	}
	
	public void cancelWaitForSubscribers() {
		Endpoint endpoint = This.getEndpoint().withPort(synchronizerPort);
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CANCEL);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = This.getCom().createRequestSocket(endpoint.toString());
		requestSocket.request(request);
			
		// Terminate the socket.
		requestSocket.terminate();
	}

	public void send(byte[] data) {
		
		publisher.sendMore(Messages.Event.STREAM);
		publisher.send(data, 0);
	}
	
	public void send(String data) {
		
		byte[] result = Messages.serialize(data);
		
		publisher.sendMore(Messages.Event.STREAM);
		publisher.send(result, 0);
	}
	
	
	public void sendTwoParts(byte[] data1, byte[] data2) {
		
		publisher.sendMore(Messages.Event.STREAM);
		publisher.sendMore(data1);
		publisher.send(data2, 0);
	}
	
	public void sendEnd() {
		
		if (!ended) {
			publisher.sendMore(Messages.Event.ENDSTREAM);
			publisher.send(Messages.Event.ENDSTREAM);
			
			ended = true;
		}
	}

	public boolean isEnded() {
		return ended;
	}
	
	public void terminate() {

		waiting.remove();
		sendEnd();
		
		context.destroySocket(publisher);
		
		JSONObject request = Messages.createTerminatePublisherRequest(This.getId(), name);
		JSONObject response = This.getCom().requestJSON(request);
		
		int value = JSON.getInt(response, Messages.RequestResponse.VALUE);
		if (value == -1) {
			System.err.println("Cannot terminate publisher");
		}
	}
	
	private Zmq.Msg processSyncRequest() {
		// send a dummy SYNC message by the publisher socket
		publisher.sendMore(Messages.Event.SYNC);
		publisher.send(Messages.Event.SYNC);
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add("Connection OK");
				
		return reply;
	}
	
	private Zmq.Msg processSubscribePublisherRequest() {
	
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Messages.RequestResponse.VALUE, 0);
		response.put(Messages.RequestResponse.MESSAGE, "OK");
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(response));
		
		return message;
	}
	
	@Override
	public String toString() {
		return "pub." + name + ":" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
	
}