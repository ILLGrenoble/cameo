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

import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.Application.This;
import fr.ill.ics.cameo.base.impl.ContextImpl;
import fr.ill.ics.cameo.base.impl.RequestSocket;
import fr.ill.ics.cameo.base.impl.ThisImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class RequesterImpl {

	public static final String REQUESTER_PREFIX = "req.";

	private ThisImpl application;
	private int requesterPort;
	private String name;
	private int responderId;
	
	// Need for a unique id per Application instance.
	private int requesterId;
	private static AtomicInteger requesterCounter = new AtomicInteger();
	
	private Zmq.Context context;
	private Zmq.Socket requester;
	private RequestSocket requestSocket;
	
	private boolean canceled = false;
	private RequesterWaitingImpl waiting = new RequesterWaitingImpl(this);
		
	public RequesterImpl(Endpoint endpoint, int requesterPort, int responderPort, String name, int responderId, int requesterId) {
		this.requesterPort = requesterPort;
		this.name = name;
		this.responderId = responderId;
		this.requesterId = requesterId;
		this.context = ((ContextImpl)This.getCom().getContext()).getContext();

		// Create the REQ socket.
		String responderEndpoint = endpoint.withPort(responderPort).toString();
		requestSocket = This.getCom().createRequestSocket(responderEndpoint);
		
		// Create the REP socket.
		requester = context.createSocket(Zmq.REP);
		requester.bind("tcp://*:" + requesterPort);
		
		waiting.add();
	}
	
	public String getName() {
		return name;
	}
	
	public static int newRequesterId() {
		return requesterCounter.incrementAndGet();
	}
	
	public static String getRequesterPortName(String name, int responderId, int requesterId) {
		return REQUESTER_PREFIX + name + "." + responderId + "." + requesterId;
	}
		
	public void send(byte[] requestData) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.REQUEST);
		request.put(Messages.Request.APPLICATION_NAME, This.getName());
		request.put(Messages.Request.APPLICATION_ID, This.getId());
		request.put(Messages.Request.SERVER_URL, This.getEndpoint().getProtocol() + "://" + This.getEndpoint().getAddress());
		request.put(Messages.Request.SERVER_PORT, This.getEndpoint().getPort());
		request.put(Messages.Request.REQUESTER_PORT, requesterPort);
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(request));
		
		// Set request in the next frame.
		message.add(requestData);
		
		requestSocket.request(message);
	}
	
	public void send(String request) {
		send(Messages.serialize(request));
	}
	
	public void sendTwoParts(byte[] requestData1, byte[] requestData2) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.REQUEST);
		request.put(Messages.Request.APPLICATION_NAME, This.getName());
		request.put(Messages.Request.APPLICATION_ID, This.getId());
		request.put(Messages.Request.SERVER_URL, This.getEndpoint().getProtocol() + "://" + This.getEndpoint().getAddress());
		request.put(Messages.Request.SERVER_PORT, This.getEndpoint().getPort());
		request.put(Messages.Request.REQUESTER_PORT, requesterPort);
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(request));
		
		// Set request1 and request2 in the next frames.
		message.add(requestData1);
		message.add(requestData2);
		
		requestSocket.request(message);
	}

	public byte[] receive() {
		
		Zmq.Msg message = null;
		
		try {
			message = Zmq.Msg.recvMsg(requester);

			if (message == null) {
				return null;
			}
			
			// Get the JSON request object.
			JSONObject request = This.getCom().parse(message.getFirstData());
			
			// Get the type.
			long type = JSON.getLong(request, Messages.TYPE);
						
			if (type == Messages.RESPONSE) {
				return message.getLastData();
			}
			else if (type == Messages.CANCEL) {
				canceled = true;
				return null;
			}
			else {
				return null;
			}
		}
		finally {
			if (message != null) {
				message.destroy();
			}
			
			// Send to the responder
			Zmq.Msg reply = new Zmq.Msg();
			reply.add("OK");
			reply.send(requester);
		}
	}

	public String receiveString() {
		return Messages.parseString(receive());
	}
	
	public void cancel() {
		
		Endpoint endpoint = This.getEndpoint().withPort(requesterPort);

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CANCEL);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = This.getCom().createRequestSocket(endpoint.toString());
		requestSocket.request(request);
		
		// Terminate the socket.
		requestSocket.terminate();
	}

	public boolean isCanceled() {
		return canceled;
	}
	
	public void terminate() {
		
		waiting.remove();
		
		// Terminate the request socket.
		requestSocket.terminate();
		
		context.destroySocket(requester);
		
		try {
			This.getCom().removePort(getRequesterPortName(name, responderId, requesterId));
			
		} catch (Exception e) {
			System.err.println("Cannot terminate requester: " + e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return REQUESTER_PREFIX + name + "." + requesterId + ":" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
	
}