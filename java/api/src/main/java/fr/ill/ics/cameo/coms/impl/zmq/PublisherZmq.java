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
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.coms.impl.PublisherImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class PublisherZmq implements PublisherImpl {

	private int publisherPort;
	private int synchronizerPort;
	private String name;
	private int numberOfSubscribers;
	private Zmq.Context context;
	private Zmq.Socket publisher = null;
	private Zmq.Socket synchronizer = null;
	private boolean ended = false;
	
	public PublisherZmq(String name, int numberOfSubscribers) {
		this.name = name;
		this.numberOfSubscribers = numberOfSubscribers;
	}
	
	public void init() {
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		publisher = context.createSocket(Zmq.PUB);
		
		String endpointPrefix = "tcp://*:";	
		
		// Loop to find an available port for the publisher.
		while (true) {
		
			int port = This.getCom().requestPort();
			String pubEndpoint = endpointPrefix + port;

			try {
				publisher.bind(pubEndpoint);
				publisherPort = port;
				break;
			}
			catch (Exception e) {
				This.getCom().setPortUnavailable(port);
			}
		}
			
		// Define the synchronizer if the number of subscribers is strictly positive.
		if (numberOfSubscribers > 0) {

			synchronizer = context.createSocket(Zmq.REP);

			// Loop to find an available port for the synchronizer.
			while (true) {
		
				int port = This.getCom().requestPort();
				String pubEndpoint = endpointPrefix + port;
		
				try {
					synchronizer.bind(pubEndpoint);
					synchronizerPort = port;
					break;
				}
				catch (Exception e) {
					This.getCom().setPortUnavailable(port);
				}
			}
		}	
	}
	
	public int getPublisherPort() {
		return publisherPort;
	}
	
	public int getSynchronizerPort() {
		return synchronizerPort; 	
	}
			
	public boolean waitForSubscribers() {
				
		if (numberOfSubscribers <= 0) {
			return true;
		}
		
		boolean canceled = false;
		
		try {
			// Loop until the number of subscribers is reached.
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
						reply = responseToSyncRequest();						
					}
					else if (type == SUBSCRIBE_PUBLISHER) {
						counter++;
						reply = responseToSubscribeRequest();
					}
					else if (type == Messages.CANCEL) {
						canceled = true;
						counter = numberOfSubscribers;
						reply = responseToCancelRequest();
					}
					else {
						reply = responseToUnknownRequest();
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
			// Destroy synchronizer socket as we do not need it anymore.
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
		RequestSocket requestSocket = This.getCom().createRequestSocket(endpoint.toString(), "zzzZZZ");
		JSONObject response = requestSocket.requestJSON(request);
		
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
			publisher.sendMore(Messages.Event.ENDSTREAM_temp);
			publisher.send("endstream");
			
			ended = true;
		}
	}

	public boolean isEnded() {
		return ended;
	}
	
	public void terminate() {
		
		sendEnd();
		
		context.destroySocket(publisher);
		
		// Release the publisher port.
		This.getCom().releasePort(publisherPort);
		
		if (synchronizer != null) {
			context.destroySocket(synchronizer);
			
			// Release the publisher port.
			This.getCom().releasePort(synchronizerPort);	
		}
	}
	
	private Zmq.Msg responseToSyncRequest() {
		
		// send a dummy SYNC message by the publisher socket
		publisher.sendMore(Messages.Event.SYNC);
		publisher.send("sync");
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(Messages.createRequestResponse(0, "OK")));
				
		return message;
	}
	
	private Zmq.Msg responseToSubscribeRequest() {
	
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(Messages.createRequestResponse(0, "OK")));
		
		return message;
	}
	
	private Zmq.Msg responseToCancelRequest() {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(Messages.createRequestResponse(0, "OK")));
		
		return message;
	}
	
	private Zmq.Msg responseToUnknownRequest() {
	
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(Messages.createRequestResponse(-1, "Unknown request")));
		
		return message;
	}
	
}