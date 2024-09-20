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

package eu.ill.cameo.api.coms.impl.zmq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.Timeout;
import eu.ill.cameo.api.base.TimeoutCounter;
import eu.ill.cameo.api.base.impl.zmq.ContextZmq;
import eu.ill.cameo.api.coms.impl.RequesterImpl;
import eu.ill.cameo.com.Zmq;
import eu.ill.cameo.common.messages.JSON;
import eu.ill.cameo.common.messages.Messages;
import eu.ill.cameo.common.strings.Endpoint;

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
	
	private void createSocket() {
		
		// Create the socket dealer.
		// The dealer socket can receive multiple response.
		// It also does not require to provide the identity of the recipient socket that should be done with a socket router.
		requester = context.createSocket(Zmq.DEALER);
		
		requester.connect(endpoint.toString());
		
		//TODO Shall we set linger to 0?
	}
	
	private void createAndSyncSocket(TimeoutCounter timeoutCounter) {
		
		// Memorize the timeout that can have been set before init().
		int previousTimeout = timeout;
		
		timeout = SYNC_TIMEOUT;
		
		while (true) {
			
			// Init the socket.
			createSocket();
			
			// Send sync returns false if a timeout occurred.
			if (sendSync()) {
				break;
			}

			// Reset the socket in case of timeout.
			resetSocket();
			
			// Increase timeout.
			timeout += SYNC_TIMEOUT;
			
			// Check the global timeout.
			if (timeoutCounter.remains() == 0) {
				throw new Timeout();
			}
		}
		
		// Reset timeout.
		timeout = previousTimeout;
	}
	

	private boolean initSocket() {
		
		// Reset timedout.
		timedout.set(false);
		
		if (requester == null) {
			try {
				createAndSyncSocket(new TimeoutCounter(timeout));
			}
			catch (Timeout e) {
				// Timeout. As initSocket() is called in sendRequest, we prefer to not throw a timeout exception.
				timedout.set(true);
				
				// Init failed.
				return false;
			}
		}
		
		return true;
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
	
	public void init(Endpoint endpoint, String responderIdentity, TimeoutCounter timeoutCounter) {
		
		this.endpoint = endpoint;
		this.responderIdentity = responderIdentity;
		
		// Get the context.
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		
		createAndSyncSocket(timeoutCounter);
	}
	
	private Zmq.Msg createMessage() {
		
		Zmq.Msg message = new Zmq.Msg();
		
		// Start with an empty message for the dealer socket. The identity of the connected router is added by the dealer socket.
		message.add(new byte[0]);
		message.add(responderIdentity);
		message.add(new byte[0]);
	
		return message;
	}
	
	private void sendRequest(byte[] part) {
		
		// Init the socket if necessary.
		if (initSocket()) {
			
			// Prepare and send the message.
			Zmq.Msg message = createMessage();
			message.add(part);
			
			message.send(requester);
		}
	}
	
	private void sendRequest(byte[] part1, byte[] part2) {
		
		// Init the socket if necessary.
		if (initSocket()) {
			
			// Prepare and send the message.
			Zmq.Msg message = createMessage();
			message.add(part1);
			message.add(part2);
			
			message.send(requester);
		}
	}
	
	private void sendRequest(byte[] part1, byte[] part2, byte[] part3) {
		
		// Init the socket if necessary.
		if (initSocket()) {
			
			// Prepare and send the message.
			Zmq.Msg message = createMessage();
			message.add(part1);
			message.add(part2);
			message.add(part3);
			
			message.send(requester);
		}
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

		// Create the poller.
		Zmq.Poller poller = this.context.createPoller(1);
		poller.register(requester);
		
		// Infinite loop if timeout is 0 or finite loop if timeout is defined.
		int i = 0;
		while (i < n || timeout == 0) {

			// Check if the requester has been canceled.
			if (canceled.get()) {
				return null;
			}

			// Poll the requester.
			poller.poll(pollingTime);
			if (poller.pollin(0)) {
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
			JSONObject request = This.getCom().parse(data[3]);
			
			// Get the type.
			long type = JSON.getLong(request, Messages.TYPE);
						
			if (type == Messages.RESPONSE) {
				return data[4];
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