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

package fr.ill.ics.cameo.coms.multi.impl.zmq;

import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.IdGenerator;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.coms.multi.Request;
import fr.ill.ics.cameo.coms.multi.impl.ResponderImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;

public class ResponderZmq implements ResponderImpl {

	private Zmq.Context context;
	private Zmq.Socket responder;
	private String cancelEndpoint;
	private Zmq.Msg reply = null; // Memorize the reply before sending it.
	
	private AtomicBoolean canceled = new AtomicBoolean(false);
	
	public void init(String endpoint) {
		
		// Create a socket REP.
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		responder = context.createSocket(Zmq.REP);
		
		// Connect to the dealer.
		responder.connect(endpoint);
		
		cancelEndpoint = "inproc://" + IdGenerator.newStringId();
		responder.bind(cancelEndpoint);
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
		
				// Get the identity of the proxy.
				byte[] responderIdentity = data[0];
		
				// Prepare the reply.
				reply = new Zmq.Msg();
				
				// Add the necessary parts.
				reply.add(responderIdentity);
				reply.add(new byte[0]);
				
				// Get the JSON request object.
				JSONObject request = This.getCom().parse(data[2]);
				
				// Get the type.
				long type = JSON.getLong(request, Messages.TYPE);
				
				if (type == Messages.REQUEST) {
	
					String name = JSON.getString(request, Messages.Request.APPLICATION_NAME);
					int id = JSON.getInt(request, Messages.Request.APPLICATION_ID);
					String serverEndpoint = JSON.getString(request, Messages.Request.SERVER_ENDPOINT);
					int serverProxyPort = JSON.getInt(request, Messages.Request.SERVER_PROXY_PORT);
					
					byte[] messagePart1 = data[3];
					byte[] messagePart2 = null;
					if (data.length > 4) {
						messagePart2 = data[4];
					}
					
					// Return the request but do not reply to the client now. This will be done by the Request.			
					return new Request(name, id, serverEndpoint, serverProxyPort, messagePart1, messagePart2);
				}
				else if (type == Messages.CANCEL) {
					canceled.set(true);
	
					// Reply immediately.
					responseToRequest(reply);
					reply.send(responder);
					reply = null;
					
					return null;
				}
				else if (type == Messages.SYNC) {
					
					// Reply immediately.
					responseToRequest(reply);
					reply.send(responder);
					reply = null;
					
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
		
		// The reply has already been created at the reception of the request.
		reply.add(part1);
		reply.add(part2);
		
		reply.send(responder);
		reply = null;
	}
	
	public void cancel() {
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Messages.CANCEL);
		
		// Create the request socket connected directly to the responder. We can create it here because it should be called only once.
		RequestSocket requestSocket = This.getCom().createRequestSocket(cancelEndpoint, "");
		requestSocket.requestJSON(jsonRequest);
		
		// Terminate the socket.
		requestSocket.terminate();
	}

	private void responseToRequest(Zmq.Msg reply) {
		reply.add(Messages.serialize(Messages.createRequestResponse(0, "OK")));
	}
		
	public boolean isCanceled() {
		return canceled.get();
	}
	
	public void terminate() {
		context.destroySocket(responder);
	}
	
	
}