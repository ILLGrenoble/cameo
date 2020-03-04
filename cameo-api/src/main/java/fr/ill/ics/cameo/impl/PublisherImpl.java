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

import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.proto.Messages.CancelPublisherSyncCommand;
import fr.ill.ics.cameo.proto.Messages.MessageType;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;
import fr.ill.ics.cameo.proto.Messages.RequestResponse;

public class PublisherImpl {

	private static final String SYNC = "SYNC";
	private static final String STREAM = "STREAM";
	private static final String ENDSTREAM = "ENDSTREAM";
	
	private ThisImpl application;
	Zmq.Context context;
	private int publisherPort;
	private int synchronizerPort;
	private String name;
	private int numberOfSubscribers;
	private Zmq.Socket publisher = null;
	private boolean ended = false;
	private PublisherWaitingImpl waiting = new PublisherWaitingImpl(this);
	
	public PublisherImpl(ThisImpl application, Zmq.Context context, int publisherPort, int synchronizerPort, String name, int numberOfSubscribers) {
		this.application = application;
		this.context = context;
		this.publisherPort = publisherPort;
		this.synchronizerPort = synchronizerPort;
		this.name = name;
		this.numberOfSubscribers = numberOfSubscribers;

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
		
					// check there are not 2 frames
					if (message.size() != 2) {
						System.err.println("unexpected number of frames, should be 2");
						continue;
					}
					// 2 frames, get first frame (type)
					byte[] typeData = message.getFirstData();
					// get last frame
					byte[] messageData = message.getLastData();
					
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
		Zmq.Msg request = application.createRequest(Type.CANCEL);
		CancelPublisherSyncCommand command = CancelPublisherSyncCommand.newBuilder().build();
		request.add(command.toByteArray());
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = application.createRequestSocket(endpoint);
			
		requestSocket.request(request);
			
		// Terminate the socket.
		requestSocket.terminate();
	}

	public void send(byte[] data) {
		
		publisher.sendMore(STREAM);
		publisher.send(data, 0);
	}
	
	public void send(String data) {
		
		byte[] result = Buffer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result, 0);
	}
	
	public void send(int[] data) {
		
		byte[] result = Buffer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result, 0);
	}

	public void send(long[] data) {

		byte[] result = Buffer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result, 0);
	}
	
	public void send(float[] data) {

		byte[] result = Buffer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result, 0);
	}
	
	public void send(double[] data) {

		byte[] result = Buffer.serialize(data);
		
		publisher.sendMore(STREAM);
		publisher.send(result, 0);
	}
	
	public void sendTwoParts(byte[] data1, byte[] data2) {
		
		publisher.sendMore(STREAM);
		
		// Send with the flag '2' which is the value for more - same in jzmq and jeromq.
		publisher.send(data1, 2);
		publisher.send(data2, 0);
	}
	
	public void sendEnd() {
		
		if (!ended) {
			publisher.sendMore(ENDSTREAM);
			publisher.send("endstream");
			
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
		
		try {
			application.destroyPublisher(name);
		} catch (Exception e) {
			System.err.println("cannot destroy publisher: " + e.getMessage());
		}
	}
	
	private Zmq.Msg processInitCommand() {
		// send a dummy SYNC message by the publisher socket
		publisher.sendMore(SYNC);
		publisher.send("sync");
		
		Zmq.Msg reply = new Zmq.Msg();
		reply.add("Connection OK");
				
		return reply;
	}
	
	private Zmq.Msg processSubscribePublisherCommand() {
	
		RequestResponse response = RequestResponse.newBuilder().setValue(0).setMessage("OK").build();
				
		Zmq.Msg reply = new Zmq.Msg();
		reply.add(response.toByteArray());
			
		return reply;
	}
	
	@Override
	public String toString() {
		return "pub." + name + ":" + application.getName() + "." + application.getId() + "@" + application.getEndpoint();
	}
	
}