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

import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.EventStreamSocket;
import fr.ill.ics.cameo.SocketException;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.Zmq;
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
		this.context = new Zmq.Context();
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

		Zmq.Msg request = createInitRequest();
		Zmq.Msg reply = null;
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
		
		Zmq.Msg request = createInitRequest();
		Zmq.Msg reply = null;
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

		Zmq.Msg request = createShowStatusRequest();
		RequestResponse requestResponse = null;
		
		try {
			Zmq.Msg reply = tryRequest(request);
	
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
		
		subscriber.connect(getCancelEndpoint());
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
	protected Zmq.Msg tryRequest(Zmq.Msg request, String endpoint, int overrideTimeout) throws ConnectionTimeout, SocketException {
		
		Zmq.Socket socket = context.createSocket(Zmq.REQ);
		
		try {
			try {
				socket.connect(endpoint);
			}
			catch (Exception e) {
				throw new SocketException(e.getMessage());
			}
			
			// send request, wait safely for reply
			Zmq.Msg msg = request.duplicate();
			msg.send(socket);
			
			int usedTimeout = timeout;
			if (overrideTimeout > -1) {
				usedTimeout = overrideTimeout;
			}
			
			if (usedTimeout > 0) {
			
//				PollItem[] items = { new PollItem(socket, ZMQ.Poller.POLLIN) };
//				ZMQ.poll(items, usedTimeout);
//				Zmq.Msg reply = null;
//				
//				// in case a response is returned before timeout
//				if (items[0].isReadable()) {
//					reply = Zmq.Msg.recvMsg(socket);
//	
//				} else {
//					throw new ConnectionTimeout();
//				}
				
				Zmq.Poller poller = context.createPoller(socket);
				Zmq.Msg reply = null;
				if (poller.poll(usedTimeout)) {
					reply = Zmq.Msg.recvMsg(socket);
				}
				else {
					throw new ConnectionTimeout();
				}
		
				return reply;
				
			} else {
				// direct receive
				Zmq.Msg reply = Zmq.Msg.recvMsg(socket);
				
				return reply;
			}
			
		} finally {
			// it is better to call destroySocket rather than socket.close()
			// it is really important to destroy the socket because Java will do it later
			// with the garbage collector
			context.destroySocket(socket);
		}
	}
	
	protected Zmq.Msg tryRequest(Zmq.Msg request, String endpoint) throws ConnectionTimeout {
		return tryRequest(request, endpoint, -1);
	}
	
	protected Zmq.Msg tryRequest(Zmq.Msg request, int overrideTimeout) throws ConnectionTimeout {
		return tryRequest(request, serverEndpoint, overrideTimeout); 
	}
	
	protected Zmq.Msg tryRequest(Zmq.Msg request) throws ConnectionTimeout {
		return tryRequest(request, serverEndpoint, -1); 
	}
	
	protected RequestSocket createSocket(String endpoint) throws SocketException {
	
		Zmq.Socket socket = context.createSocket(Zmq.REQ);
		
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
	protected Zmq.Msg createInitRequest() {
		
		Zmq.Msg request = createRequest(Type.INIT);
		Init start = Init.newBuilder().build();
		request.add(start.toByteArray());
		
		return request;
	}
	
	/**
	 * create show status request
	 * 
	 * @return request
	 */
	protected Zmq.Msg createShowStatusRequest() {
		
		Zmq.Msg request = createRequest(Type.STATUS);
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
	protected Zmq.Msg createGetStatusRequest(int id) {
		
		Zmq.Msg request = createRequest(Type.GETSTATUS);
		GetStatusCommand command = GetStatusCommand.newBuilder().setId(id).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	/**
	 * create startedUnmanaged request
	 * @param pid 
	 * 
	 * @param text
	 * @return
	 */
	protected Zmq.Msg createStartedUnmanagedRequest(String name, long pid) {
		
		Zmq.Msg request = createRequest(Type.STARTEDUNMANAGED);
		StartedUnmanagedCommand command = StartedUnmanagedCommand.newBuilder()
												.setName(name)
												.setPid(pid)
												.build();
		request.add(command.toByteArray());
		
		return request;
	}
	
	/**
	 * create terminatedUnmanaged request
	 * 
	 * @param text
	 * @return
	 */
	protected Zmq.Msg createTerminatedUnmanagedRequest(int id) {
		
		Zmq.Msg request = createRequest(Type.TERMINATEDUNMANAGED);
		TerminatedUnmanagedCommand command = TerminatedUnmanagedCommand.newBuilder().setId(id).build();
		request.add(command.toByteArray());
		
		return request;
	}
	
}