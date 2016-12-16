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

import java.net.UnknownHostException;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.InvalidProtocolBufferException;

import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.EventStreamSocket;
import fr.ill.ics.cameo.SocketException;
import fr.ill.ics.cameo.UnexpectedException;
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
	protected ZContext context;
	protected int timeout = 0; // default value because of ZeroMQ design
	protected String cancelEndpoint;
	
	protected static final String STREAM = "STREAM";
	protected static final String ENDSTREAM = "ENDSTREAM";
	protected static final String STATUS = "STATUS";
	protected static final String RESULT = "RESULT";
	protected static final String PUBLISHER = "PUBLISHER";
	protected static final String PORT = "PORT";
	protected static final String CANCEL = "CANCEL";
	
	protected String getCancelEndpoint() {
		return cancelEndpoint;
	}
	
	final protected void init() {
		this.context = new ZContext();
		cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
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
		// destroying the context
		context.destroy();
	}
	
	/**
	 * test connection with server
	 * @param timeout
	 * 
	 */
	public boolean isAvailable(int overrideTimeout) {

		ZMsg request = createInitRequest();
		ZMsg reply = null;
		try {
			reply = tryRequest(request, overrideTimeout);
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
	
	protected void sendInit(String endpoint) {
		
		ZMsg request = createInitRequest();
		ZMsg reply = null;
		try {
			reply = tryRequest(request, endpoint);
			reply.destroy();
			request.destroy();

		} catch (ConnectionTimeout e) {
			// do nothing
		}
	}
	
	protected void sendInit() {
		sendInit(serverEndpoint);
	}
	
	/**
	 * 
	 * @throws ConnectionTimeout 
	 */
	protected EventStreamSocket openEventStream() {

		ZMsg request = createShowStatusRequest();
		RequestResponse requestResponse = null;
		
		try {
			ZMsg reply = tryRequest(request);
	
			byte[] messageData = reply.getFirst().getData();
			requestResponse = RequestResponse.parseFrom(messageData);
			
		} catch (InvalidProtocolBufferException e) {
			throw new UnexpectedException("Cannot parse response");
		} catch (Exception e) {
			return null;
		}
		
		// Prepare our context and subscriber
		Socket subscriber = context.createSocket(ZMQ.SUB);
		
		statusPort = requestResponse.getValue();
		
		subscriber.connect(url + ":" + statusPort);
		subscriber.subscribe(STATUS.getBytes());
		subscriber.subscribe(RESULT.getBytes());
		subscriber.subscribe(PUBLISHER.getBytes());
		subscriber.subscribe(PORT.getBytes());
		
		subscriber.connect(getCancelEndpoint());
		subscriber.subscribe(CANCEL.getBytes());
		
		// polling to wait for connection
		PollItem[] items = { new PollItem(subscriber, ZMQ.Poller.POLLIN) };
		
		// the server returns a STATUS message that is used to synchronize the subscriber
		sendInit();

		if (timeout > 0) {
			ZMQ.poll(items, timeout);
			
			if (!items[0].isReadable()) {
				return null;
			}
					
		} else {
			// direct receive
			ZMsg reply = ZMsg.recvMsg(subscriber);
		}

		return new EventStreamSocket(context, subscriber);
	}

	/**
	 * send request
	 * 
	 * @param request
	 * @return reply
	 * @throws ConnectionTimeout 
	 * @throws SocketException 
	 */
	protected ZMsg tryRequest(ZMsg request, String endpoint, int overrideTimeout) throws ConnectionTimeout, SocketException {
		
		Socket socket = context.createSocket(ZMQ.REQ);
		
		try {
			try {
				socket.connect(endpoint);
			}
			catch (Exception e) {
				throw new SocketException(e.getMessage());
			}
			
			// send request, wait safely for reply
			ZMsg msg = request.duplicate();
			msg.send(socket);
			
			int usedTimeout = timeout;
			if (overrideTimeout > -1) {
				usedTimeout = overrideTimeout;
			}
			
			if (usedTimeout > 0) {
			
				PollItem[] items = { new PollItem(socket, ZMQ.Poller.POLLIN) };
				ZMQ.poll(items, usedTimeout);
				ZMsg reply = null;
				
				// in case a response is returned before timeout
				if (items[0].isReadable()) {
					reply = ZMsg.recvMsg(socket);
	
				} else {
					throw new ConnectionTimeout();
				}
		
				return reply;
				
			} else {
				// direct receive
				ZMsg reply = ZMsg.recvMsg(socket);
				
				return reply;
			}
			
		} finally {
			// it is better to call destroySocket rather than socket.close()
			// it is really important to destroy the socket because Java will do it later
			// with the garbage collector
			context.destroySocket(socket);
		}
	}
	
	protected ZMsg tryRequest(ZMsg request, String endpoint) throws ConnectionTimeout {
		return tryRequest(request, endpoint, -1);
	}
	
	protected ZMsg tryRequest(ZMsg request, int overrideTimeout) throws ConnectionTimeout {
		return tryRequest(request, serverEndpoint, overrideTimeout); 
	}
	
	protected ZMsg tryRequest(ZMsg request) throws ConnectionTimeout {
		return tryRequest(request, serverEndpoint, -1); 
	}
	
	protected RequestSocket createSocket(String endpoint) throws SocketException {
	
		Socket socket = context.createSocket(ZMQ.REQ);
		
		try {
			socket.connect(endpoint);
		}
		catch (Exception e) {
			throw new SocketException(e.getMessage());
		}
		
		return new RequestSocket(context, socket, timeout);
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	protected ZMsg createRequest(Type type) {
		
		ZMsg request = new ZMsg();
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
	protected ZMsg createInitRequest() {
		
		ZMsg request = createRequest(Type.INIT);
		Init start = Init.newBuilder().build();
		request.add(start.toByteArray());
		
		return request;
	}
	
	/**
	 * create show status request
	 * 
	 * @return request
	 */
	protected ZMsg createShowStatusRequest() {
		
		ZMsg request = createRequest(Type.STATUS);
		String content = "status";
		request.add(content);
		
		return request;
	}
	
	/**
	 * create getStatus request
	 * 
	 * @param text
	 * @return
	 */
	protected ZMsg createGetStatusRequest(int id) {
		
		ZMsg request = createRequest(Type.GETSTATUS);
		GetStatusCommand command = GetStatusCommand.newBuilder().setId(id).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	/**
	 * create startedUnmanaged request
	 * 
	 * @param text
	 * @return
	 */
	protected ZMsg createStartedUnmanagedRequest(String name) {
		
		ZMsg request = createRequest(Type.STARTEDUNMANAGED);
		StartedUnmanagedCommand command = StartedUnmanagedCommand.newBuilder().setName(name).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	/**
	 * create terminatedUnmanaged request
	 * 
	 * @param text
	 * @return
	 */
	protected ZMsg createTerminatedUnmanagedRequest(int id) {
		
		ZMsg request = createRequest(Type.TERMINATEDUNMANAGED);
		TerminatedUnmanagedCommand command = TerminatedUnmanagedCommand.newBuilder().setId(id).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
}