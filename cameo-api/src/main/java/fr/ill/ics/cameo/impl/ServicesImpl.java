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

import org.json.simple.JSONObject;

import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.EventStreamSocket;
import fr.ill.ics.cameo.SocketException;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.proto.Messages.GetStatusCommand;
import fr.ill.ics.cameo.proto.Messages.Init;
import fr.ill.ics.cameo.proto.Messages.MessageType;
import fr.ill.ics.cameo.proto.Messages.MessageType.Type;
import fr.ill.ics.cameo.proto.Messages.RequestResponse;
import fr.ill.ics.cameo.proto.Messages.StartedUnmanagedCommand;
import fr.ill.ics.cameo.proto.Messages.TerminatedUnmanagedCommand;

public class ServicesImpl {

	protected String serverEndpoint;
	protected String url;
	protected int port;
	protected int statusPort;
	protected Zmq.Context context;
	protected int timeout = 0; // default value because of ZeroMQ design
	protected RequestSocket requestSocket;
	
	protected static final String STREAM = "STREAM";
	protected static final String ENDSTREAM = "ENDSTREAM";
	protected static final String STATUS = "STATUS";
	protected static final String RESULT = "RESULT";
	protected static final String PUBLISHER = "PUBLISHER";
	protected static final String PORT = "PORT";
	protected static final String CANCEL = "CANCEL";
	
	/**
	 * Initializes the context and the request socket. The serverEndpoint must have been set.
	 */
	final protected void init() {
		this.context = new Zmq.Context();
		this.requestSocket = this.createRequestSocket(serverEndpoint);
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public String getEndpoint() {
		return serverEndpoint;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getStatusEndpoint() {
		return url + ":" + statusPort;
	}
	
	public void terminate() {
		
		// Terminate the request socket.
		requestSocket.terminate();
		
		// destroying the context
		context.destroy();
	}
	
	/**
	 * test connection with server
	 * @param timeout
	 * 
	 */
	public boolean isAvailable(int overrideTimeout) {

		Zmq.Msg request = createSyncRequest();
		Zmq.Msg reply = null;
		try {
			reply = requestSocket.request(request, overrideTimeout);
			reply.destroy();
			request.destroy();
			return true;

		} catch (ConnectionTimeout e) {
			// do nothing, timeout
		} catch (Exception e) {
			// do nothing
		}
		
		return false;
	}
	
	protected void sendInit() {
		
		Zmq.Msg request = createSyncRequest();
		Zmq.Msg reply = null;
		try {
			reply = requestSocket.request(request);
			reply.destroy();
			request.destroy();

		} catch (ConnectionTimeout e) {
			// do nothing
		}
	}
	
	/**
	 * 
	 * @throws ConnectionTimeout 
	 */
	protected EventStreamSocket openEventStream() {

		Zmq.Msg request = createShowStatusRequest();
		RequestResponse requestResponse = null;
		
		try {
			Zmq.Msg reply = requestSocket.request(request);
			byte[] messageData = reply.getFirstData();
			requestResponse = RequestResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		} catch (Exception e) {
			return null;
		}
		
		// Prepare our context and subscriber
		Zmq.Socket subscriber = context.createSocket(Zmq.SUB);
		
		statusPort = requestResponse.getValue();
		
		subscriber.connect(url + ":" + statusPort);
		subscriber.subscribe(STATUS);
		subscriber.subscribe(RESULT);
		subscriber.subscribe(PUBLISHER);
		subscriber.subscribe(PORT);
		
		String cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(CANCEL);
		
		// polling to wait for connection
		Zmq.Poller poller = context.createPoller(subscriber);
		
		while (true) {
			
			// the server returns a STATUS message that is used to synchronize the subscriber
			sendInit();

			// return at the first response.
			if (poller.poll(100)) {
				break;
			}
		}
		
		Zmq.Socket cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);
		
		return new EventStreamSocket(context, subscriber, cancelPublisher);
	}
	
	protected RequestSocket createRequestSocket(String endpoint) throws SocketException {
		
		RequestSocket requestSocket = new RequestSocket(context, timeout);
		requestSocket.connect(endpoint);
		
		return requestSocket;
	}
		
	/**
	 * 
	 * @param type
	 * @return
	 */
	protected Zmq.Msg createRequest(Type type) {
		
		Zmq.Msg request = new Zmq.Msg();
		// add the message type on the first frame
		MessageType messageType = MessageType.newBuilder().setType(type).build();
		request.add(messageType.toByteArray());
		
		return request;
	}
	
	/**
	 * create init request
	 * 
	 * @param text
	 * @return
	 */
	protected Zmq.Msg createSyncRequest() {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.SYNC);
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(request.toJSONString().getBytes(Message.CHARSET));

		return message;
	}
	
	/**
	 * create show status request
	 * 
	 * @return request
	 */
	protected Zmq.Msg createShowStatusRequest() {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.STATUS);

		Zmq.Msg message = new Zmq.Msg();
		message.add(request.toJSONString().getBytes(Message.CHARSET));
		
		return message;
	}
	
	/**
	 * create getStatus request
	 * 
	 * @param text
	 * @return
	 */
	protected Zmq.Msg createGetStatusRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.GET_STATUS);
		request.put(Message.GetStatusRequest.ID, id);

		Zmq.Msg message = new Zmq.Msg();
		message.add(request.toJSONString().getBytes(Message.CHARSET));
		
		return message;	
	}
	
	/**
	 * create startedUnmanaged request
	 * @param pid 
	 * 
	 * @param text
	 * @return
	 */
	protected Zmq.Msg createStartedUnmanagedRequest(String name, long pid) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.STARTED_UNMANAGED);
		request.put(Message.StartedUnmanagedRequest.NAME, name);
		request.put(Message.StartedUnmanagedRequest.PID, pid);
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(request.toJSONString().getBytes(Message.CHARSET));
		
		return message;
	}
	
	/**
	 * create terminatedUnmanaged request
	 * 
	 * @param text
	 * @return
	 */
	protected Zmq.Msg createTerminatedUnmanagedRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.TERMINATED_UNMANAGED);
		request.put(Message.TerminatedUnmanagedRequest.ID, id);
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(request.toJSONString().getBytes(Message.CHARSET));
		
		return message;
	}
	
}