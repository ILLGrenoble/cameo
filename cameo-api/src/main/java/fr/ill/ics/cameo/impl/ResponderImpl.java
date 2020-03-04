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

import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.proto.Messages.MessageType;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;

public class ResponderImpl {

	public static final String RESPONDER_PREFIX = "rep.";

	private ApplicationImpl application;
	Zmq.Context context;
	private int responderPort;
	private String name;
	private Zmq.Socket responder;
	
	private boolean ended = false;
	private boolean canceled = false;
	private ResponderWaitingImpl waiting = new ResponderWaitingImpl(this);
	
	public ResponderImpl(ApplicationImpl application, Zmq.Context context, int responderPort, String name) {
		this.application = application;
		this.context = context;
		this.responderPort = responderPort;
		this.name = name;

		// create a socket REP
		responder = context.createSocket(Zmq.REP);
		responder.bind("tcp://*:" + responderPort);
		
		waiting.add();
	}
	
	public String getName() {
		return name;
	}

	public RequestImpl receive() {
		
		Zmq.Msg message = null;
		Zmq.Msg reply = null;
		
		try {
			message = Zmq.Msg.recvMsg(responder);

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
			byte[] typeData = message.getFirstData();
			// Get last frame
			byte[] messageData = message.getLastData();
			
			// dispatch message
			MessageType type = MessageType.parseFrom(typeData);
			
			if (type.getType() == Type.REQUEST) {
				// Parse the message
				fr.ill.ics.cameo.proto.Messages.Request request = fr.ill.ics.cameo.proto.Messages.Request.parseFrom(messageData);
				
				// Create the request
				RequestImpl impl = new RequestImpl(application, 
						request.getApplicationName(), 
						request.getApplicationId(), 
						request.getMessage(), 
						request.getServerUrl(),
						request.getServerPort(),
						request.getRequesterPort());
				
				// Set the optional message 2.
				if (request.hasMessage2()) {
					impl.setMessage2(request.getMessage2());
				}
				
				return impl;
			}
			else if (type.getType() == Type.CANCEL) {
				canceled = true;
				
				return null;
			}
			
		} catch (InvalidProtocolBufferException e) {
			System.err.println("problem in parsing of message");
			
		} finally {
			
			if (message != null) {
				message.destroy();
			}	

			// Send to the requester
			reply = new Zmq.Msg();
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

		Zmq.Msg requestMessage = application.createRequest(Type.CANCEL);
		String content = "cancel";
		requestMessage.add(content);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = application.createRequestSocket(endpoint);
		
		requestSocket.request(requestMessage);
		
		// Terminate the socket.
		requestSocket.terminate();
	}

	public boolean isEnded() {
		return ended;
	}
	
	public boolean isCanceled() {
		return canceled;
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