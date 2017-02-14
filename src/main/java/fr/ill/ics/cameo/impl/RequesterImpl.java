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

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.proto.Messages.MessageType;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;
import fr.ill.ics.cameo.proto.Messages.Request;

public class RequesterImpl {

	public static final String REQUESTER_PREFIX = "req.";

	private ApplicationImpl application;
	ZContext context;
	private String responderEndpoint;
	private int requesterPort;
	private String name;
	private int responderId;
	private Socket requester = null;
	private RequesterWaitingImpl waiting = new RequesterWaitingImpl(this);
	
	public RequesterImpl(ApplicationImpl application, ZContext context, String url, int requesterPort, int responderPort, String name, int responderId) {
		this.application = application;
		this.context = context;
		this.requesterPort = requesterPort;
		this.responderEndpoint = url + ":" + responderPort;
		this.name = name;
		this.responderId = responderId;

		// create a socket REP
		requester = context.createSocket(ZMQ.REP);
		requester.bind("tcp://*:" + requesterPort);
		
		waiting.add();
	}
	
	public String getName() {
		return name;
	}
	
	public static String getRequesterPortName(String name, int responderId) {
		return REQUESTER_PREFIX + name + "." + responderId;
	}
	
	private void send(ByteString request) {
		ZMsg requestMessage = application.createRequest(Type.REQUEST);
		String requesterEndpoint = application.getUrl() + ":" + requesterPort;
		
		Request command = Request.newBuilder()
										.setApplicationId(application.getId())
										.setMessage(request)
										.setEndpoint(requesterEndpoint)
										.build();
		requestMessage.add(command.toByteArray());
		
		application.tryRequest(requestMessage, responderEndpoint);
	}
	
	private void send(ByteString request1, ByteString request2) {
		ZMsg requestMessage = application.createRequest(Type.REQUEST);
		String requesterEndpoint = application.getUrl() + ":" + requesterPort;
		
		Request command = Request.newBuilder()
										.setApplicationId(application.getId())
										.setMessage(request1)
										.setMessage2(request2)
										.setEndpoint(requesterEndpoint)
										.build();
		requestMessage.add(command.toByteArray());
		
		application.tryRequest(requestMessage, responderEndpoint);
	}
	
	public void send(byte[] request) {
		send(ByteString.copyFrom(request));
	}
	
	public void send(String request) {
		send(ByteString.copyFromUtf8(request));
	}
	
	public void sendTwoParts(byte[] request1, byte[] request2) {
		send(ByteString.copyFrom(request1), ByteString.copyFrom(request2));
	}

	public byte[] receive() {
		
		ZMsg message = null;
		
		try {
			message = ZMsg.recvMsg(requester);

			if (message == null) {
				return null;
			}
			
			// 2 frames, get first frame (type)
			byte[] typeData = message.getFirst().getData();
			// Get last frame
			byte[] messageData = message.getLast().getData();
						
			// dispatch message
			MessageType type = MessageType.parseFrom(typeData);
						
			if (type.getType() == Type.RESPONSE) {
				return messageData;
			
			} else if (type.getType() == Type.CANCEL) {
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
			ZMsg reply = new ZMsg();
			reply.add("OK");
			reply.send(requester);
		}
	}

	public String receiveString() {
		return Buffer.parseString(receive());
	}
	
	public void cancel() {
		
		String endpoint = application.getUrl() + ":" + requesterPort;

		ZMsg requestMessage = application.createRequest(Type.CANCEL);
		String content = "cancel";
		requestMessage.add(content);
		
		application.tryRequest(requestMessage, endpoint);
	}

	public void terminate() {
		
		waiting.remove();
		context.destroySocket(requester);
		
		try {
			application.removePort(getRequesterPortName(name, responderId));
			
		} catch (Exception e) {
			System.err.println("Cannot terminate requester: " + e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return REQUESTER_PREFIX + name + ":" + application.getName() + "." + application.getId() + "@" + application.getEndpoint();
	}
	
}