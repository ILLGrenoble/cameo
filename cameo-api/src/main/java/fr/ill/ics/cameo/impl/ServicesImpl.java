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
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.ConnectionTimeout;
import fr.ill.ics.cameo.EventStreamSocket;
import fr.ill.ics.cameo.SocketException;
import fr.ill.ics.cameo.UnexpectedException;
import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Socket;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Message;
import fr.ill.ics.cameo.strings.Endpoint;

public class ServicesImpl {

	protected Endpoint serverEndpoint;
	protected int[] serverVersion = new int[3];
	protected int statusPort;
	protected Zmq.Context context;
	protected int timeout = 0; // default value because of ZeroMQ design
	protected RequestSocket requestSocket;
	protected JSON.ConcurrentParser parser = new JSON.ConcurrentParser();
	
	/**
	 * Initializes the context and the request socket. The serverEndpoint must have been set.
	 */
	final protected void init() {
		this.context = new Zmq.Context();
		this.requestSocket = this.createRequestSocket(serverEndpoint.toString());
	}
	
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public Endpoint getEndpoint() {
		return serverEndpoint;
	}
		
	public int[] getVersion() {
		return serverVersion;
	}
	
	public Endpoint getStatusEndpoint() {
		return serverEndpoint.withPort(statusPort);
	}
	
	public void destroySocket(Socket socket) {
		context.destroySocket(socket);
	}
	
	public void terminate() {
		
		// Terminate the request socket.
		requestSocket.terminate();
		
		// destroying the context
		context.destroy();
	}
	
	public JSONObject parse(Zmq.Msg reply) throws ParseException {
		return parser.parse(Message.parseString(reply.getFirstData()));
	}
	
	public JSONObject parse(byte[] data) throws ParseException {
		return parser.parse(Message.parseString(data));
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
	
	protected void sendSync() {
		
		Zmq.Msg request = createSyncRequest();
		try {
			Zmq.Msg reply = requestSocket.request(request);
			reply.destroy();
			request.destroy();

		} catch (ConnectionTimeout e) {
			// do nothing
		}
	}
	
	protected void sendSyncStream(String name) {
		
		Zmq.Msg request = createSyncStreamRequest(name);
		try {
			Zmq.Msg reply = requestSocket.request(request);
			reply.destroy();
			request.destroy();

		} catch (ConnectionTimeout e) {
			// do nothing
		}
	}

	protected void retrieveServerVersion() {
		
		Zmq.Msg request = createVersionRequest();
		Zmq.Msg reply = requestSocket.request(request);
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			serverVersion[0] = JSON.getInt(response, Message.VersionResponse.MAJOR);
			serverVersion[1] = JSON.getInt(response, Message.VersionResponse.MINOR);
			serverVersion[2] = JSON.getInt(response, Message.VersionResponse.REVISION);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	/**
	 * 
	 * @throws ConnectionTimeout 
	 */
	protected EventStreamSocket openEventStream() {

		Zmq.Msg request = createStreamStatusRequest();
		
		Zmq.Msg reply = requestSocket.request(request);
		JSONObject response;
		
		try {
			// Get the JSON response object.
			response = parse(reply);
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
		
		// Prepare our subscriber.
		Zmq.Socket subscriber = context.createSocket(Zmq.SUB);
		
		statusPort = JSON.getInt(response, Message.RequestResponse.VALUE);
		
		subscriber.connect(getStatusEndpoint().toString());
		subscriber.subscribe(Message.Event.STATUS);
		subscriber.subscribe(Message.Event.RESULT);
		subscriber.subscribe(Message.Event.PUBLISHER);
		subscriber.subscribe(Message.Event.PORT);
		subscriber.subscribe(Message.Event.KEYVALUE);
		
		String cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Message.Event.CANCEL);
		
		// polling to wait for connection
		Zmq.Poller poller = context.createPoller(subscriber);
		
		while (true) {
			
			// the server returns a STATUS message that is used to synchronize the subscriber
			sendSync();

			// return at the first response.
			if (poller.poll(100)) {
				break;
			}
		}
		
		Zmq.Socket cancelPublisher = context.createSocket(Zmq.PUB);
		cancelPublisher.bind(cancelEndpoint);
		
		return new EventStreamSocket(this, subscriber, cancelPublisher);
	}
	
	protected RequestSocket createRequestSocket(String endpoint) throws SocketException {
		
		RequestSocket requestSocket = new RequestSocket(context, timeout);
		requestSocket.connect(endpoint);
		
		return requestSocket;
	}
	
	protected Zmq.Msg message(JSONObject object) {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Message.serialize(object));

		return message;
	}
	
	/**
	 * create init request
	 * 
	 * @return
	 */
	protected Zmq.Msg createSyncRequest() {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.SYNC);
		
		return message(request);
	}
	

	protected Zmq.Msg createSyncStreamRequest(String name) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.SYNC_STREAM);
		request.put(Message.SyncStreamRequest.NAME, name);
		
		return message(request);
	}
	
	/**
	 * create version request
	 * 
	 * @return request
	 */
	protected Zmq.Msg createVersionRequest() {

		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.IMPL_VERSION);

		return message(request);
	}
	
	/**
	 * create show status request
	 * 
	 * @return request
	 */
	protected Zmq.Msg createStreamStatusRequest() {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.STATUS);

		return message(request);
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

		return message(request);	
	}
	
	/**
	 * create startedUnmanaged request
	 * @param pid 
	 * 
	 * @param text
	 * @return
	 */
	protected Zmq.Msg createAttachUnmanagedRequest(String name, long pid) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.ATTACH_UNMANAGED);
		request.put(Message.AttachUnmanagedRequest.NAME, name);
		request.put(Message.AttachUnmanagedRequest.PID, pid);
		
		return message(request);
	}
	
	/**
	 * create terminatedUnmanaged request
	 * 
	 * @param text
	 * @return
	 */
	protected Zmq.Msg createDetachUnmanagedRequest(int id) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.DETACH_UNMANAGED);
		request.put(Message.DetachUnmanagedRequest.ID, id);
		
		return message(request);
	}
	
}