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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.proto.Messages.MessageType;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;
import fr.ill.ics.cameo.proto.Messages.Request;

public class RequesterImpl {

	public static final String REQUESTER_PREFIX = "req.";

	private ApplicationImpl application;
	Zmq.Context context;
	private String responderEndpoint;
	private int requesterPort;
	private String name;
	private int responderId;
	
	// Need for a unique id per Application instance.
	private int requesterId;
	private static AtomicInteger requesterCounter = new AtomicInteger();
	
	private Zmq.Socket requester = null;
	private boolean canceled = false;
	private RequesterWaitingImpl waiting = new RequesterWaitingImpl(this);
		
	public RequesterImpl(ApplicationImpl application, Zmq.Context context, String url, int requesterPort, int responderPort, String name, int responderId, int requesterId) {
		this.application = application;
		this.context = context;
		this.requesterPort = requesterPort;
		this.responderEndpoint = url + ":" + responderPort;
		this.name = name;
		this.responderId = responderId;
		this.requesterId = requesterId;

		// create a socket REP
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
	
	private void send(ByteString request) {
		Zmq.Msg requestMessage = application.createRequest(Type.REQUEST);
		
		Request command = Request.newBuilder()
										.setApplicationName(application.getName())
										.setApplicationId(application.getId())
										.setMessage(request)
										.setServerUrl(application.getUrl())
										.setServerPort(application.getPort())
										.setRequesterPort(requesterPort)
										.build();
		requestMessage.add(command.toByteArray());
		
		application.tryRequest(requestMessage, responderEndpoint);
	}
	
	private void send(ByteString request1, ByteString request2) {
		Zmq.Msg requestMessage = application.createRequest(Type.REQUEST);
		
		Request command = Request.newBuilder()
										.setApplicationName(application.getName())						
										.setApplicationId(application.getId())
										.setMessage(request1)
										.setMessage2(request2)
										.setServerUrl(application.getUrl())
										.setServerPort(application.getPort())
										.setRequesterPort(requesterPort)
										.build();
		requestMessage.add(command.toByteArray());
		
		application.tryRequest(requestMessage, responderEndpoint);
	}
	
	public void send(byte[] request) {
		send(ByteString.copyFrom(request));
	}
	
	public void send(String request) {
		send(Buffer.serialize(request));
	}
	
	public void sendTwoParts(byte[] request1, byte[] request2) {
		send(ByteString.copyFrom(request1), ByteString.copyFrom(request2));
	}

	public byte[] receive() {
		
		Zmq.Msg message = null;
		
		try {
			message = Zmq.Msg.recvMsg(requester);

			if (message == null) {
				return null;
			}
			
			// 2 frames, get first frame (type)
			byte[] typeData = message.getFirstData();
			// Get last frame
			byte[] messageData = message.getLastData();
						
			// dispatch message
			MessageType type = MessageType.parseFrom(typeData);
						
			if (type.getType() == Type.RESPONSE) {
				return messageData;
			
			} else if (type.getType() == Type.CANCEL) {
				canceled = true;
				return null;
				
			} else {
				return null;
			}
			
		} catch (InvalidProtocolBufferException e) {
			System.err.println("problem in parsing of message");
			return null;
			
		} finally {
			
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
		return Buffer.parseString(receive());
	}
	
	public void cancel() {
		
		String endpoint = application.getUrl() + ":" + requesterPort;

		Zmq.Msg requestMessage = application.createRequest(Type.CANCEL);
		String content = "cancel";
		requestMessage.add(content);
		
		application.tryRequest(requestMessage, endpoint);
	}

	public boolean isCanceled() {
		return canceled;
	}
	
	public void terminate() {
		
		waiting.remove();
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