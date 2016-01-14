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

import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.proto.Messages.CancelPublisherSyncCommand;
import fr.ill.ics.cameo.proto.Messages.MessageType;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;
import fr.ill.ics.cameo.proto.Messages.RequestResponse;

public class PublisherImpl {

	private static final String SYNC = "SYNC";
	private static final String STREAM = "STREAM";
	private static final String ENDSTREAM = "ENDSTREAM";
	
	private ApplicationImpl application;
	ZContext context;
	private int publisherPort;
	private int synchronizerPort;
	private String name;
	private int numberOfSubscribers;
	private Socket publisher = null;
	private boolean ended = false;
	private PublisherWaitingImpl waiting = new PublisherWaitingImpl(this);
	
	public PublisherImpl(ApplicationImpl application, ZContext context, int publisherPort, int synchronizerPort, String name, int numberOfSubscribers) {
		this.application = application;
		this.context = context;
		this.publisherPort = publisherPort;
		this.synchronizerPort = synchronizerPort;
		this.name = name;
		this.numberOfSubscribers = numberOfSubscribers;

		// create a socket for publishing
		publisher = context.createSocket(ZMQ.PUB);
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
		
		Socket synchronizer = null;
		boolean canceled = false;
		
		try {
			// create a socket to receive the messages from the subscribers
			synchronizer = context.createSocket(ZMQ.REP);
			String endpoint = application.getUrl() + ":" + synchronizerPort;
			synchronizer.bind(endpoint);
			
			// loop until the number of subscribers is reached
			int counter = 0;
			
			while (counter < numberOfSubscribers) {

				ZMsg message = null;
				ZMsg reply = null;
				
				try {
					message = ZMsg.recvMsg(synchronizer);
					
					if (message == null) {
						break;
					}
		
					// check there are not 2 frames
					if (message.size() != 2) {
						System.err.println("unexpected number of frames, should be 2");
						continue;
					}
					// 2 frames, get first frame (type)
					byte[] typeData = message.getFirst().getData();
					// get last frame
					byte[] messageData = message.getLast().getData();
					
					// dispatch message
					MessageType type = MessageType.parseFrom(typeData);
					
					if (type.getType() == Type.INIT) {
						reply = processInitCommand();						
						
					} else if (type.getType() == Type.SUBSCRIBEPUBLISHER) {
						counter++;
						reply = processSubscribePublisherCommand();
						
					} else if (type.getType() == Type.CANCEL) {
						canceled = true;
						counter = numberOfSubscribers;
						message.send(synchronizer);
						
					} else {
						System.err.println("unknown message type " + type.getType());
						message.send(synchronizer);
					}
					
					// send to the client
					if (reply != null) {
						reply.send(synchronizer);
					}
					
				} catch (InvalidProtocolBufferException e) {
					throw new UnexpectedException("Cannot parse response");
					
				} finally {
					
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
		String endpoint = application.getUrl() + ":" + (publisherPort + 1);
		ZMsg request = application.createRequest(Type.CANCEL);
		CancelPublisherSyncCommand command = CancelPublisherSyncCommand.newBuilder().build();
		request.add(command.toByteArray());
		
		try {
			ZMsg reply = application.tryRequest(request, endpoint);
			byte[] messageData = reply.getFirst().getData();
			RequestResponse requestResponse = null;

			requestResponse = RequestResponse.parseFrom(messageData);
		
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}

	public void send(byte[] data) {
		publisher.sendMore(STREAM);
		publisher.send(data);
	}
	
	public void send(String data) {
		
		byte[] result = Serializer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result);
	}
	
	public void send(int[] data) {
		
		byte[] result = Serializer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result);
	}

	public void send(long[] data) {

		byte[] result = Serializer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result);
	}
	
	public void send(float[] data) {

		byte[] result = Serializer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result);
	}
	
	public void send(double[] data) {

		byte[] result = Serializer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result);
	}
	

	public void sendEnd() {
		
		if (!ended) {
			publisher.sendMore(ENDSTREAM);
			publisher.send("endstream");
			
			ended = true;
		}
	}

	public boolean hasEnded() {
		return ended;
	}
	
	public void terminate() {

		waiting.remove();
		sendEnd();
		
		context.destroySocket(publisher);
		
		try {
			application.destroyPublisher(name);
		} catch (Exception e) {
			System.err.println("cannot destroy publisher: " + e.getMessage());
		}
	}
	
	private ZMsg processInitCommand() {
		// send a dummy SYNC message by the publisher socket
		publisher.sendMore(SYNC);
		publisher.send("sync");
		
		ZMsg reply = new ZMsg();
		reply.add("Connection OK");
		
		return reply;
	}
	
	private ZMsg processSubscribePublisherCommand() {
	
		RequestResponse response = RequestResponse.newBuilder().setValue(0).setMessage("OK").build();
				
		ZMsg reply = new ZMsg();
		reply.add(response.toByteArray());
			
		return reply;
	}
	
	@Override
	public String toString() {
		return "pub." + name + ":" + application.getName() + "." + application.getId() + "@" + application.getEndpoint();
	}
	
}