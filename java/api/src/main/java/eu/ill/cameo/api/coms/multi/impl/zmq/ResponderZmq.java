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

package eu.ill.cameo.api.coms.multi.impl.zmq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.IdGenerator;
import eu.ill.cameo.api.base.RequestSocket;
import eu.ill.cameo.api.base.This;
import eu.ill.cameo.api.base.impl.zmq.ContextZmq;
import eu.ill.cameo.api.coms.multi.Request;
import eu.ill.cameo.api.coms.multi.impl.ResponderImpl;
import eu.ill.cameo.com.Zmq;
import eu.ill.cameo.common.messages.JSON;
import eu.ill.cameo.common.messages.Messages;

public class ResponderZmq implements ResponderImpl {

	private Zmq.Context context;
	private Zmq.Socket responder;
	private String cancelEndpoint;
	
	private final static int HEADER_SIZE = 5;
	private byte[][] requestHeader = new byte[HEADER_SIZE][];
	
	private AtomicBoolean canceled = new AtomicBoolean(false);
	
	public void init(String endpoint) {
		
		// Create a socket router.
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		responder = context.createSocket(Zmq.ROUTER);
		
		// Connect to the dealer.
		responder.connect(endpoint);
		
		cancelEndpoint = "inproc://" + IdGenerator.newStringId();
		responder.bind(cancelEndpoint);
	}

	private void copyHeader(byte[][] data) {
		for (int i = 0; i < HEADER_SIZE; ++i) {
			
			if (i < data.length) {
				requestHeader[i] = data[i];
			}
			else {
				requestHeader[i] = new byte[0];
			}
		}
	}
	
	private Request processCancel() {
		canceled.set(true);
		
		// Reply immediately.
		replyOK();
		
		return null;
	}
	
	private Request processRequest(JSONObject request, byte[][] data) {
		
		String name = JSON.getString(request, Messages.Request.APPLICATION_NAME);
		int id = JSON.getInt(request, Messages.Request.APPLICATION_ID);
		String serverEndpoint = JSON.getString(request, Messages.Request.SERVER_ENDPOINT);
		int serverProxyPort = JSON.getInt(request, Messages.Request.SERVER_PROXY_PORT);
		
		byte[] messagePart1 = data[HEADER_SIZE + 1];
		byte[] messagePart2 = null;
		if (data.length > HEADER_SIZE + 2) {
			messagePart2 = data[HEADER_SIZE + 2];
		}
		
		// Return the request but do not reply to the client now. This will be done by the Request.			
		return new Request(name, id, serverEndpoint, serverProxyPort, messagePart1, messagePart2);
	}
	
	public Request receive() {
		
		// Loop on the SYNC messages because they are not requests.
		while (true) {
		
			Zmq.Msg message = null;
			
			try {
				message = Zmq.Msg.recvMsg(responder);
	
				if (message == null) {
					return null;
				}
				
				// Get all the parts. 
				byte[][] data = message.getAllData();

				// Memorize the header to reuse when replying.
				copyHeader(data);
				
				// Special case for cancel messages which are shorter.
				if (data.length < HEADER_SIZE + 1) {
					return processCancel();
				}
				
				// Get the JSON request object.
				JSONObject request = This.getCom().parse(data[HEADER_SIZE]);
				
				// Get the type.
				long type = JSON.getLong(request, Messages.TYPE);
				
				if (type == Messages.REQUEST) {
					return processRequest(request, data); 
					
				}
				else if (type == Messages.CANCEL) {
					return processCancel();
				}
				else if (type == Messages.SYNC) {
					
					// Reply immediately.
					replyOK();
					
					// Do not return, continue the loop.
				}
			}
			finally {
				if (message != null) {
					message.destroy();
				}	
			}
		}
	}
	
	public void reply(byte[] part1, byte[] part2) {
		
		Zmq.Msg reply = new Zmq.Msg();
		
		for (int i = 0; i < HEADER_SIZE; ++i) {
			reply.add(requestHeader[i]);
		}
				
		reply.add(part1);
		reply.add(part2);
		
		reply.send(responder);
	}

	private void replyOK() {
		
		Zmq.Msg reply = new Zmq.Msg();
		
		for (int i = 0; i < HEADER_SIZE; ++i) {
			reply.add(requestHeader[i]);
		}
		
		reply.add(Messages.serialize(Messages.createRequestResponse(0, "OK")));
		
		reply.send(responder);
	}
	
	public void cancel() {
		
		if (canceled.get()) {
			return;
		}
		
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Messages.CANCEL);
		
		// Create the request socket connected directly to the responder. We can create it here because it should be called only once.
		RequestSocket requestSocket = This.getCom().createRequestSocket(cancelEndpoint, "");
		requestSocket.requestJSON(jsonRequest);
		
		// Terminate the socket.
		requestSocket.terminate();
	}
		
	public boolean isCanceled() {
		return canceled.get();
	}
	
	public void terminate() {
		context.destroySocket(responder);
	}
	
	
}