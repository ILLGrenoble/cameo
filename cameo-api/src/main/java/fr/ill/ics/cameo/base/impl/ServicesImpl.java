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

package fr.ill.ics.cameo.base.impl;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.Zmq.Msg;
import fr.ill.ics.cameo.Zmq.Socket;
import fr.ill.ics.cameo.base.ConnectionTimeout;
import fr.ill.ics.cameo.base.EventStreamSocket;
import fr.ill.ics.cameo.base.SocketException;
import fr.ill.ics.cameo.base.UnexpectedException;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
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
	
	public Zmq.Context getContext() {
		return context;
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
		return parser.parse(Messages.parseString(reply.getFirstData()));
	}
	
	public JSONObject parse(byte[] data) throws ParseException {
		return parser.parse(Messages.parseString(data));
	}
	
	/**
	 * test connection with server
	 * @param timeout
	 * 
	 */
	public boolean isAvailable(int overrideTimeout) {

		try {
			requestSocket.request(Messages.createSyncRequest(), overrideTimeout);
			return true;

		} catch (ConnectionTimeout e) {
			// do nothing, timeout
		} catch (Exception e) {
			// do nothing
		}
		
		return false;
	}
	
	protected void sendSync() {
		
		try {
			requestSocket.request(Messages.createSyncRequest());

		} catch (ConnectionTimeout e) {
			// do nothing
		}
	}
	
	protected void sendSyncStream(String name) {
		
		try {
			requestSocket.request(Messages.createSyncStreamRequest(name));

		} catch (ConnectionTimeout e) {
			// do nothing
		}
	}

	protected void retrieveServerVersion() {
		
		Zmq.Msg reply = requestSocket.request(Messages.createVersionRequest());
		
		try {
			// Get the JSON response object.
			JSONObject response = parse(reply);
		
			serverVersion[0] = JSON.getInt(response, Messages.VersionResponse.MAJOR);
			serverVersion[1] = JSON.getInt(response, Messages.VersionResponse.MINOR);
			serverVersion[2] = JSON.getInt(response, Messages.VersionResponse.REVISION);
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

		Zmq.Msg reply = requestSocket.request(Messages.createStreamStatusRequest());
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
		
		statusPort = JSON.getInt(response, Messages.RequestResponse.VALUE);
		
		subscriber.connect(getStatusEndpoint().toString());
		subscriber.subscribe(Messages.Event.STATUS);
		subscriber.subscribe(Messages.Event.RESULT);
		subscriber.subscribe(Messages.Event.PUBLISHER);
		subscriber.subscribe(Messages.Event.PORT);
		subscriber.subscribe(Messages.Event.KEYVALUE);
		
		String cancelEndpoint = "inproc://cancel." + CancelIdGenerator.newId();
		
		subscriber.connect(cancelEndpoint);
		subscriber.subscribe(Messages.Event.CANCEL);
		
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
	
	
	public RequestSocket createRequestSocket(String endpoint) throws SocketException {
		
		RequestSocket requestSocket = new RequestSocket(context, timeout);
		requestSocket.connect(endpoint);
		
		return requestSocket;
	}
	
}