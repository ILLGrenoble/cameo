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

import fr.ill.ics.cameo.Application;
import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Context;
import fr.ill.ics.cameo.proto.Messages;
import fr.ill.ics.cameo.proto.Messages.RequestResponse;

public class SubscriberImpl {

	private static final String SYNC = "SYNC";
	private static final String STREAM = "STREAM";
	private static final String ENDSTREAM = "ENDSTREAM";
	private static final String CANCEL = "CANCEL";
	private static final String STATUS = "STATUS";
	
	private ServerImpl server;
	private Zmq.Context context;
	private String url;
	private int publisherPort;
	private int synchronizerPort;
	private Zmq.Socket subscriber;
	private String cancelEndpoint;
	private Zmq.Socket cancelPublisher;
	private String publisherName;
	private int numberOfSubscribers;
	private InstanceImpl instance;
	private boolean endOfStream = false;
	private SubscriberWaitingImpl waiting = new SubscriberWaitingImpl(this);
	
	SubscriberImpl(ServerImpl server, Zmq.Context context, String url, int publisherPort, int synchronizerPort, String publisherName, int numberOfSubscribers, InstanceImpl instance) {
		this.server = server;
		this.context = context;
		this.url = url;
		this.publisherPort = publisherPort;
		this.synchronizerPort = synchronizerPort;
		this.publisherName = publisherName;
		this.numberOfSubscribers = numberOfSubscribers;
		this.instance = instance;
		
		waiting.add();
	}
	
	void init() throws ConnectionTimeout {
		
		// Create the subscriber
		subscriber = context.createSocket(Zmq.SUB);
		
		subscriber.connect(url + ":" + publisherPort);
		subscriber.subscribe(SYNC);
		subscriber.subscribe(STREAM);
		subscriber.subscribe(ENDSTREAM);
		
		// Create an endpoint that should be unique
		cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		// Create a cancel publisher so that it sends the CANCEL message to the status subscriber (connected to 2 publishers)
		cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);

		// Subscribe to CANCEL
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(CANCEL);

		// Subscribe to STATUS
		subscriber.connect(instance.getStatusEndpoint());
		subscriber.subscribe(STATUS);
		
		// Synchronize the subscriber only if the number of subscribers > 0
		if (numberOfSubscribers > 0) {
			
			String endpoint = url + ":" + synchronizerPort;
			
			// Create a socket that will be used for several requests.
			RequestSocket requestSocket = server.createSocket(endpoint);
						
			
			
//			// Polling to wait for connection
//			PollItem[] items = { new PollItem(subscriber, ZMQ.Poller.POLLIN) };
//			
//			boolean ready = false;
//			while (!ready) {
//				// The subscriber sends init messages to the publisher that returns SYNC message
//				Zmq.Msg request = server.createInitRequest();
//				Zmq.Msg reply = null;
//				try {
//					reply = requestSocket.request(request);
//					reply.destroy();
//					request.destroy();
//
//				} catch (ConnectionTimeout e) {
//					// do nothing
//				}
//	
//				// Polling until the first SYNC message is received
//				ZMQ.poll(items, 100);
//				
//				if (items[0].isReadable()) {
//					ready = true;
//				}
//			}
			
			
			// polling to wait for connection
			Zmq.Poller poller = new Zmq.Poller(subscriber);
			
			boolean ready = false;
			while (!ready) {
				
				// The subscriber sends init messages to the publisher that returns SYNC message
				Zmq.Msg request = server.createInitRequest();
				Zmq.Msg reply = null;
				try {
					reply = requestSocket.request(request);
					reply.destroy();
					request.destroy();

				} catch (ConnectionTimeout e) {
					// do nothing
				}

				// return at the first response.
				if (poller.poll(100)) {
					ready = true;
				}
			}
			
			// The subscriber is connected and ready to receive data.
			// Notify the publisher that it can send data.
			Zmq.Msg request = server.createSubscribePublisherRequest();
			Zmq.Msg reply = requestSocket.request(request);
			
			byte[] messageData = reply.getFirstData();
			RequestResponse requestResponse = null;
			
			try {
				requestResponse = RequestResponse.parseFrom(messageData);
				
			} catch (InvalidProtocolBufferException e) {
				throw new UnexpectedException("Cannot parse response");
			}

			
			requestSocket.terminate();
		}
	}
	
	public String getPublisherName() { 
		return publisherName;
	}
	
	public String getInstanceName() {
		return instance.getName();
	}
	
	public int getInstanceId() {
		return instance.getId();
	}
	
	public String getInstanceEndpoint() {
		return instance.getEndpoint();
	}
	
	public boolean hasEnded() {
		return endOfStream;
	}
	
	/**
	 * 
	 * @return the byte[] data. If the return value is null, then the stream is finished. 
	 */
	public byte[] receive() {

		while (true) {
			String response = subscriber.recvStr();
			
			if (response.equals(STREAM)) {
				return subscriber.recv();
				
			} else if (response.equals(ENDSTREAM)) {
				endOfStream = true;
				return null;
				
			} else if (response.equals(CANCEL)) {
				return null;
				
			} else if (response.equals(STATUS)) {
				byte[] statusResponse = subscriber.recv();
				
				try {
					Messages.StatusEvent protoStatus = Messages.StatusEvent.parseFrom(statusResponse);
					
					if (instance.getId() == protoStatus.getId()) {
						
						// Get the state
						int state = protoStatus.getApplicationState();
						
						// Test if the state is terminal
						if (state == Application.State.SUCCESS 
								|| state == Application.State.STOPPED
								|| state == Application.State.KILLED
								|| state == Application.State.ERROR) {
							// Exit because the remote application has terminated.
							return null;
						}
					}
					
				} catch (InvalidProtocolBufferException e) {
					throw new UnexpectedException("Cannot parse response");
				}
			}
		}
	}
	
	/**
	 * 
	 * @return the byte[] data. If the return value is null, then the stream is finished. 
	 */
	public byte[][] receiveTwoParts() {

		while (true) {
			String response = subscriber.recvStr();
			
			if (response.equals(STREAM)) {
				byte[][] result = new byte[2][];
				result[0] = subscriber.recv();
				result[1] = subscriber.recv();
				
				return result;
				
			} else if (response.equals(ENDSTREAM)) {
				endOfStream = true;
				return null;
				
			} else if (response.equals(CANCEL)) {
				return null;
				
			} else if (response.equals(STATUS)) {
				byte[] statusResponse = subscriber.recv();
				
				try {
					Messages.StatusEvent protoStatus = Messages.StatusEvent.parseFrom(statusResponse);
					
					if (instance.getId() == protoStatus.getId()) {
						
						// Get the state
						int state = protoStatus.getApplicationState();
						
						// Test if the state is terminal
						if (state == Application.State.SUCCESS 
								|| state == Application.State.STOPPED
								|| state == Application.State.KILLED
								|| state == Application.State.ERROR) {
							// Exit because the remote application has terminated.
							return null;
						}
					}
					
				} catch (InvalidProtocolBufferException e) {
					throw new UnexpectedException("Cannot parse response");
				}
			}
		}
	}
	
	/**
	 * 
	 * @return the string data. If the return value is null, then the stream is finished. 
	 */
	public String receiveString() {
		
		byte[] data = receive();
		
		if (data == null) {
			return null;
		}
		
		return Buffer.parseString(data);
	}
	
	/**
	 * 
	 * @return the int[] data. If the return value is null, then the stream is finished or the data are corrupted. 
	 */
	public int[] receiveInt32() {

		byte[] data = receive();
		
		if (data == null) {
			return null;
		}
		
		return Buffer.parseInt32(data);
	}
	
	/**
	 * 
	 * @return the long[] data. If the return value is null, then the stream is finished or the data are corrupted. 
	 */
	public long[] receiveInt64() {

		byte[] data = receive();
		
		if (data == null) {
			return null;
		}
		
		return Buffer.parseInt64(data);
	}
	
	/**
	 * 
	 * @return the float[] data. If the return value is null, then the stream is finished or the data are corrupted. 
	 */
	public float[] receiveFloat() {

		byte[] data = receive();
		
		if (data == null) {
			return null;
		}
		
		return Buffer.parseFloat(data);
	}

	/**
	 * 
	 * @return the double[] data. If the return value is null, then the stream is finished or the data are corrupted. 
	 */
	public double[] receiveDouble() {

		byte[] data = receive();
		
		if (data == null) {
			return null;
		}
		
		return Buffer.parseDouble(data);
	}
	
	public void cancel() {
	
		cancelPublisher.sendMore(CANCEL);
		cancelPublisher.send("cancel");
	}
	
	public void terminate() {
		
		waiting.remove();
		
		context.destroySocket(subscriber);
		context.destroySocket(cancelPublisher);
	}

	@Override
	public String toString() {
		return "sub." + publisherName + ":" + instance.getName() + "." + instance.getId() + "@" + instance.getEndpoint();
	}
	
}