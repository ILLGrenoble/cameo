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

package fr.ill.ics.cameo.coms.basic.impl.zmq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.coms.basic.impl.RequesterImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class RequesterZmq implements RequesterImpl {

	private int pollingTime = 100;
	private int timeout = 0;
	
	private Zmq.Context context;
	private Zmq.Socket requester;
	private Endpoint endpoint;
	private String responderIdentity;
	
	private AtomicBoolean canceled = new AtomicBoolean(false);
	private AtomicBoolean timedout = new AtomicBoolean(false);
	
	private final static int SYNC_TIMEOUT = 200;
	
	public void setPollingTime(int value) {
		pollingTime = value;
	}

	public void setTimeout(int value) {
		timeout = value;
	}
	
	private void resetSocket() {

		// Destroy socket.
		if (requester != null) {
			context.destroySocket(requester);
			requester = null;
		}
	}
	
	private void initSocket() {
		
		if (requester == null) {
			// Create the REQ socket.
			requester = context.createSocket(Zmq.REQ);
			requester.connect(endpoint.toString());
			
			//TODO Shall we set linger to 0?
		}
	}
	
	private boolean sendSync() {
		
		// Create the request.
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Messages.SYNC);
	
		sendRequest(Messages.serialize(jsonRequest));
		if (receiveMessage() != null) {
			// Had a response we can exit the loop.
			return true;
		}
		
		return false;
	}
		
	public void init(Endpoint endpoint, String responderIdentity) {
		
		this.endpoint = endpoint;
		this.responderIdentity = responderIdentity;
		
		// Get the context.
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();

		// Loop to ensure that the responder is connected to the proxy and can reply.
		// Initial timeout.
		timeout = SYNC_TIMEOUT;
		
		while (true) {
			// Init the socket.
			initSocket();
			
			// Send sync returns false if a timeout occurred.
			if (sendSync()) {
				break;
			}

			// Reset the socket in case of timeout.
			resetSocket();
			
			// Increase timeout.
			timeout += SYNC_TIMEOUT;
		}
		
		// Reset timeout.
		timeout = 0;
	}
	
	private Zmq.Msg createMessage() {
		
		Zmq.Msg message = new Zmq.Msg();
		
		// Add the responder identity as first part.
		message.add(responderIdentity);
		message.add(new byte[0]);
	
		return message;
	}
	
	private void sendRequest(byte[] part) {
	
		// Reset timedout.
		timedout.set(false);
		
		// Init the socket if necessary.
		initSocket();
		
		// Prepare and send the message.
		Zmq.Msg message = createMessage();
		message.add(part);
		
		message.send(requester);
	}
	
	private void sendRequest(byte[] part1, byte[] part2) {
		
		// Reset timedout.
		timedout.set(false);
		
		// Init the socket if necessary.
		initSocket();
		
		// Prepare and send the message.
		Zmq.Msg message = createMessage();
		message.add(part1);
		message.add(part2);
		
		message.send(requester);
	}
	
	private void sendRequest(byte[] part1, byte[] part2, byte[] part3) {
	
		// Reset timedout.
		timedout.set(false);
		
		// Init the socket if necessary.
		initSocket();
		
		// Prepare and send the message.
		Zmq.Msg message = createMessage();
		message.add(part1);
		message.add(part2);
		message.add(part3);
		
		message.send(requester);
	}
	
	public void send(byte[] requestData) {
		
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Messages.REQUEST);
		jsonRequest.put(Messages.Request.APPLICATION_NAME, This.getName());
		jsonRequest.put(Messages.Request.APPLICATION_ID, This.getId());
		jsonRequest.put(Messages.Request.SERVER_ENDPOINT, This.getEndpoint().toString());
		jsonRequest.put(Messages.Request.SERVER_PROXY_PORT, This.getCom().getResponderProxyPort());
		
		sendRequest(Messages.serialize(jsonRequest), requestData);
	}
	
	public void send(String request) {
		send(Messages.serialize(request));
	}
	
	public void sendTwoParts(byte[] requestData1, byte[] requestData2) {
		
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Messages.REQUEST);
		jsonRequest.put(Messages.Request.APPLICATION_NAME, This.getName());
		jsonRequest.put(Messages.Request.APPLICATION_ID, This.getId());
		jsonRequest.put(Messages.Request.SERVER_ENDPOINT, This.getEndpoint().toString());
		jsonRequest.put(Messages.Request.SERVER_PROXY_PORT, This.getCom().getResponderProxyPort());
		
		sendRequest(Messages.serialize(jsonRequest), requestData1, requestData2);
	}

	private Zmq.Msg receiveMessage() {
		
		// Define the number of iterations.
		int n = 0;
		if (pollingTime > 0) {
			n = timeout / pollingTime + 1;
		}
		
		// Infinite loop if timeout is 0 or finite loop if timeout is defined.
		int i = 0;
		while (i < n || timeout == 0) {

			// Check if the requester has been canceled.
			if (canceled.get()) {
				return null;
			}

			// Poll the requester.
			Zmq.Poller poller = context.createPoller(requester);
			if (poller.poll(pollingTime)) {
				return Zmq.Msg.recvMsg(requester);
			}

			i++;
		}

		// Timeout occurred.
		timedout.set(true);

		// Reset the socket because it cannot be reused after a timeout.
		resetSocket();
		
		return null;
	}
	
	public byte[] receive() {
		
		Zmq.Msg message = null;
		
		try {
			message = receiveMessage();

			if (message == null) {
				return null;
			}

			// Get the data.
			byte[][] data = message.getAllData();
			
			// Get the JSON request object.
			JSONObject request = This.getCom().parse(data[2]);
			
			// Get the type.
			long type = JSON.getLong(request, Messages.TYPE);
						
			if (type == Messages.RESPONSE) {
				return data[3];
			}
			else {
				return null;
			}
		}
		finally {
			if (message != null) {
				message.destroy();
			}
		}
	}

	public String receiveString() {
		
		byte[] response = receive();
		
		if (response != null) {
			return Messages.parseString(response);
		}
		
		return null;
	}
	
	public void cancel() {
		canceled.set(true);		
	}

	public boolean isCanceled() {
		return canceled.get();
	}

	@Override
	public boolean hasTimedout() {
		return timedout.get();
	}
	
	public void terminate() {
		resetSocket();
	}
	
}