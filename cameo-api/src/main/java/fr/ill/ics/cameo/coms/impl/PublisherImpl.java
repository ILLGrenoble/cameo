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
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.UnexpectedException;
import fr.ill.ics.cameo.base.Application.This;
import fr.ill.ics.cameo.base.impl.RequestSocket;
import fr.ill.ics.cameo.base.impl.ServicesImpl;
import fr.ill.ics.cameo.base.impl.ThisImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.strings.Endpoint;

public class PublisherImpl {

	private ThisImpl application;
	private int publisherPort;
	private int synchronizerPort;
	private String name;
	private int numberOfSubscribers;
	private Zmq.Socket publisher = null;
	private boolean ended = false;
	private PublisherWaitingImpl waiting = new PublisherWaitingImpl(this);
	
	public PublisherImpl(ThisImpl application, int publisherPort, int synchronizerPort, String name, int numberOfSubscribers) {
		this.application = application;
		this.publisherPort = publisherPort;
		this.synchronizerPort = synchronizerPort;
		this.name = name;
		this.numberOfSubscribers = numberOfSubscribers;

		// create a socket for publishing
		publisher = this.application.getContext().createSocket(Zmq.PUB);
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
			synchronizer = this.application.getContext().createSocket(Zmq.REP);
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
					JSONObject request = application.parse(message);
					
					// Get the type.
					long type = JSON.getLong(request, Message.TYPE);
					
					if (type == Message.SYNC) {
						reply = processSyncRequest();						
					}
					else if (type == Message.SUBSCRIBE_PUBLISHER_v0) {
						counter++;
						reply = processSubscribePublisherRequest();
					}
					else if (type == Message.CANCEL) {
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
				catch (ParseException e) {
					throw new UnexpectedException("Cannot parse response");
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
				this.application.getContext().destroySocket(synchronizer);
			}	
		}
		
		return !canceled;
	}
	
	public void cancelWaitForSubscribers() {
		Endpoint endpoint = application.getEndpoint().withPort(publisherPort + 1);
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.CANCEL);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = application.createRequestSocket(endpoint.toString());
		requestSocket.request(request);
			
		// Terminate the socket.
		requestSocket.terminate();
	}

	public void send(byte[] data) {
		
		publisher.sendMore(Message.Event.STREAM);
		publisher.send(data, 0);
	}
	
	public void send(String data) {
		
		byte[] result = Message.serialize(data);
		
		publisher.sendMore(Message.Event.STREAM);
		publisher.send(result, 0);
	}
	
	
	public void sendTwoParts(byte[] data1, byte[] data2) {
		
		publisher.sendMore(Message.Event.STREAM);
		publisher.sendMore(data1);
		publisher.send(data2, 0);
	}
	
	public void sendEnd() {
		
		if (!ended) {
			publisher.sendMore(Message.Event.ENDSTREAM);
			publisher.send(Message.Event.ENDSTREAM);
			
			ended = true;
		}
	}

	public boolean isEnded() {
		return ended;
	}
	
	private static JSONObject createTerminatePublisherRequest(int id, String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.TERMINATE_PUBLISHER_v0);
		request.put(Message.TerminatePublisherRequest.ID, id);
		request.put(Message.TerminatePublisherRequest.NAME, name);

		return request;
	}
	
	public void terminate() {

		waiting.remove();
		sendEnd();
		
		this.application.getContext().destroySocket(publisher);
		
		JSONObject request = createTerminatePublisherRequest(This.getId(), name);
		JSONObject response = This.getCom().request(request);
		
		int value = JSON.getInt(response, Message.RequestResponse.VALUE);
		if (value == -1) {
			System.err.println("Cannot terminate publisher");
		}
	}
	
	private Zmq.Msg processSyncRequest() {
		// send a dummy SYNC message by the publisher socket
		publisher.sendMore(Message.Event.SYNC);
		publisher.send(Message.Event.SYNC);
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add("Connection OK");
				
		return reply;
	}
	
	private Zmq.Msg processSubscribePublisherRequest() {
	
		// Return the reply.
		JSONObject response = new JSONObject();
		response.put(Message.RequestResponse.VALUE, 0);
		response.put(Message.RequestResponse.MESSAGE, "OK");
		
		return application.message(response);
	}
	
	@Override
	public String toString() {
		return "pub." + name + ":" + application.getName() + "." + application.getId() + "@" + application.getEndpoint();
	}
	
}