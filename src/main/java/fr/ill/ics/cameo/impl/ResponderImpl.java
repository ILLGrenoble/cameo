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

import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.proto.Messages.MessageType;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;

public class ResponderImpl {

	public static final String RESPONDER_PREFIX = "rep.";

	private ApplicationImpl application;
	ZContext context;
	private int responderPort;
	private String name;
	private Socket responder = null;
	private boolean ended = false;
	private ResponderWaitingImpl waiting = new ResponderWaitingImpl(this);
	
	public ResponderImpl(ApplicationImpl application, ZContext context, int responderPort, String name) {
		this.application = application;
		this.context = context;
		this.responderPort = responderPort;
		this.name = name;

		// create a socket REP
		responder = context.createSocket(ZMQ.REP);
		responder.bind("tcp://*:" + responderPort);
		
		waiting.add();
	}
	
	public String getName() {
		return name;
	}

	public RequestImpl receive() {
		
		ZMsg message = null;
		ZMsg reply = null;
		
		try {
			message = ZMsg.recvMsg(responder);

			if (message == null) {
				ended = true;
				
				return null;
			}

			// Check there are not 2 frames
			if (message.size() != 2) {
				System.err.println("Unexpected number of frames, should be 2");
				ended = true;
				
				return null;
			}
			// 2 frames, get first frame (type)
			byte[] typeData = message.getFirst().getData();
			// Get last frame
			byte[] messageData = message.getLast().getData();
			
			// dispatch message
			MessageType type = MessageType.parseFrom(typeData);
			
			if (type.getType() == Type.REQUEST) {
				// Parse the message
				fr.ill.ics.cameo.proto.Messages.Request request = fr.ill.ics.cameo.proto.Messages.Request.parseFrom(messageData);
			
				// Create the request
				return new RequestImpl(application, context, request.getEndpoint(), request.getMessage(), request.getApplicationId());
				
			} else if (type.getType() == Type.CANCEL) {
				ended = true;
				
				return null;
			}
			
		} catch (InvalidProtocolBufferException e) {
			System.err.println("problem in parsing of message");
			
		} finally {
			
			if (message != null) {
				message.destroy();
			}	

			// Send to the requester
			reply = new ZMsg();
			reply.add("OK");
			reply.send(responder);
			
			if (reply != null) {
				reply.destroy();
			}
		}
			
		return null;
	}
	
	public void cancel() {
		String endpoint = application.getUrl() + ":" + responderPort;

		ZMsg requestMessage = application.createRequest(Type.CANCEL);
		String content = "cancel";
		requestMessage.add(content);
		
		application.tryRequest(requestMessage, endpoint);
	}

	public boolean hasEnded() {
		return ended;
	}
	
	public void terminate() {

		waiting.remove();
		context.destroySocket(responder);
		
		try {
			application.removePort(RESPONDER_PREFIX + name);
			
		} catch (Exception e) {
			System.err.println("Cannot terminate responder: " + e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return RESPONDER_PREFIX + name + ":" + application.getName() + "." + application.getId() + "@" + application.getEndpoint();
	}
	
}