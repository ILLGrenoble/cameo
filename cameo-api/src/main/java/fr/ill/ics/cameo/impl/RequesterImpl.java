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

import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.strings.Endpoint;

public class RequesterImpl {

	public static final String REQUESTER_PREFIX = "req.";

	private ThisImpl application;
	Zmq.Context context;
	private int requesterPort;
	private String name;
	private int responderId;
	
	// Need for a unique id per Application instance.
	private int requesterId;
	private static AtomicInteger requesterCounter = new AtomicInteger();
	
	private Zmq.Socket requester;
	private RequestSocket requestSocket;
	
	private boolean canceled = false;
	private RequesterWaitingImpl waiting = new RequesterWaitingImpl(this);
		
	public RequesterImpl(ThisImpl application, Zmq.Context context, String url, int requesterPort, int responderPort, String name, int responderId, int requesterId) {
		this.application = application;
		this.context = context;
		this.requesterPort = requesterPort;
		String responderEndpoint = url + ":" + responderPort;
		this.name = name;
		this.responderId = responderId;
		this.requesterId = requesterId;

		// Create the REQ socket.
		requestSocket = application.createRequestSocket(responderEndpoint);
		
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
		request.put(Message.TYPE, Message.REQUEST);
		request.put(Message.Request.APPLICATION_NAME, application.getName());
		request.put(Message.Request.APPLICATION_ID, application.getId());
		request.put(Message.Request.SERVER_URL, application.getEndpoint().getProtocol() + "://" + application.getEndpoint().getAddress());
		request.put(Message.Request.SERVER_PORT, application.getEndpoint().getPort());
		request.put(Message.Request.REQUESTER_PORT, requesterPort);
		
		Zmq.Msg message = application.message(request);
		
		// Set request in the next frame.
		message.add(requestData);
		
		requestSocket.request(message);
	}
	
	public void send(String request) {
		send(Message.serialize(request));
	}
	
	public void sendTwoParts(byte[] requestData1, byte[] requestData2) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.REQUEST);
		request.put(Message.Request.APPLICATION_NAME, application.getName());
		request.put(Message.Request.APPLICATION_ID, application.getId());
		request.put(Message.Request.SERVER_URL, application.getEndpoint().getProtocol() + "://" + application.getEndpoint().getAddress());
		request.put(Message.Request.SERVER_PORT, application.getEndpoint().getPort());
		request.put(Message.Request.REQUESTER_PORT, requesterPort);
		
		Zmq.Msg message = application.message(request);
		
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
			JSONObject request = application.parse(message);
			
			// Get the type.
			long type = JSON.getLong(request, Message.TYPE);
						
			if (type == Message.RESPONSE) {
				return message.getLastData();
			}
			else if (type == Message.CANCEL) {
				canceled = true;
				return null;
			}
			else {
				return null;
			}
		}
		catch (ParseException e) {
			System.err.println("Cannot parse message");
			return null;
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
		return Message.parseString(receive());
	}
	
	public void cancel() {
		
		Endpoint endpoint = application.getEndpoint().withPort(requesterPort);

		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.CANCEL);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = application.createRequestSocket(endpoint.toString());
		requestSocket.request(application.message(request));
		
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
			application.removePort(getRequesterPortName(name, responderId, requesterId));
			
		} catch (Exception e) {
			System.err.println("Cannot terminate requester: " + e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return REQUESTER_PREFIX + name + "." + requesterId + ":" + application.getName() + "." + application.getId() + "@" + application.getEndpoint();
	}
	
}