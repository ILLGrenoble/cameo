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

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.coms.basic.Request;
import fr.ill.ics.cameo.coms.basic.impl.ResponderImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;

public class ResponderZmq implements ResponderImpl {

	private int responderPort;
	
	private Zmq.Context context;
	private Zmq.Socket responder;
	private String responderIdentity;
	private Zmq.Msg reply = null; // Memorize the reply before sending it.
	
	private boolean ended = false;
	private boolean canceled = false;
	
	public void init(String responderIdentity) {
		
		this.responderIdentity = responderIdentity;
		
		// Create a socket ROUTER.
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		responder = context.createSocket(Zmq.ROUTER);
		
		// Set the identity.
		responder.setIdentity(responderIdentity);
		
		// Connect to the proxy.
		responder.connect(This.getEndpoint().toString());
		
		System.out.println("Connected responder " + responderIdentity + " to the proxy " + This.getEndpoint());
		
		String endpointPrefix = "tcp://*:";	
		
		// Loop to find an available port for the responder.
		while (true) {
		
			int port = This.getCom().requestPort();
			String pubEndpoint = endpointPrefix + port;

			try {
				responder.bind(pubEndpoint);
				responderPort = port;
				break;
			}
			catch (Exception e) {
				This.getCom().setPortUnavailable(port);
			}
		}
	}
	
	public int getResponderPort() {
		return responderPort;
	}

	public Request receive() {
		
		// Loop on the SYNC messages because they are not requests.
		while (true) {
		
			Zmq.Msg message = null;
			
			try {
				message = Zmq.Msg.recvMsg(responder);
	
				if (message == null) {
					ended = true;
					
					return null;
				}
				
				// Get all the parts. 
				byte[][] data = message.getAllData();
		
				// Get the identity of the proxy.
				byte[] proxyIdentity = data[0];
		
				// Get the identity of the requester.
				byte[] requesterIdentity = data[2];
	
				// Prepare the reply.
				reply = new Zmq.Msg();
				
				// Add the necessary parts.
				reply.add(proxyIdentity);
				reply.add(new byte[0]);
				reply.add(requesterIdentity);
				reply.add(new byte[0]);
				
				// Get the JSON request object.
				JSONObject request = This.getCom().parse(data[4]);
				
				// Get the type.
				long type = JSON.getLong(request, Messages.TYPE);
				
				if (type == Messages.REQUEST) {
	
					String name = JSON.getString(request, Messages.Request.APPLICATION_NAME);
					int id = JSON.getInt(request, Messages.Request.APPLICATION_ID);
					String serverUrl = JSON.getString(request, Messages.Request.SERVER_URL);
					int serverPort = JSON.getInt(request, Messages.Request.SERVER_PORT);
					
					byte[] messagePart1 = data[5];
					byte[] messagePart2 = null;
					if (data.length > 6) {
						messagePart2 = data[6];
					}
					
					// Return the request but do not reply to the client now. This will be done by the Request.			
					return new Request(name, id, serverUrl, serverPort, messagePart1, messagePart2);
				}
				else if (type == Messages.CANCEL) {
					canceled = true;
	
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
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CANCEL);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = This.getCom().createRequestSocket(This.getEndpoint().toString(), responderIdentity);
		requestSocket.requestJSON(request);
		
		// Terminate the socket.
		requestSocket.terminate();
	}

	private void responseToRequest(Zmq.Msg reply) {
		reply.add(Messages.serialize(Messages.createRequestResponse(0, "OK")));
	}
	
	public boolean isEnded() {
		return ended;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void terminate() {
		context.destroySocket(responder);
		
		// Release the responder port.
		This.getCom().releasePort(responderPort);
	}
	
	
}