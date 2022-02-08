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
import fr.ill.ics.cameo.strings.Endpoint;

public class ResponderZmq implements ResponderImpl {

	private int responderPort;
	
	private Zmq.Context context;
	private Zmq.Socket responder;
	
	private boolean ended = false;
	private boolean canceled = false;
	
	public void init() {
		
		// Create a socket REP.
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();
		responder = context.createSocket(Zmq.REP);
		
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
		
		Zmq.Msg message = null;
		Zmq.Msg reply = null;
		
		try {
			message = Zmq.Msg.recvMsg(responder);

			if (message == null) {
				ended = true;
				
				return null;
			}
			
			// Get the JSON request object.
			JSONObject request = This.getCom().parse(message.getFirstData());
			
			// Get the type.
			long type = JSON.getLong(request, Messages.TYPE);
			
			if (type == Messages.REQUEST) {

				String name = JSON.getString(request, Messages.Request.APPLICATION_NAME);
				int id = JSON.getInt(request, Messages.Request.APPLICATION_ID);
				String serverUrl = JSON.getString(request, Messages.Request.SERVER_URL);
				int serverPort = JSON.getInt(request, Messages.Request.SERVER_PORT);
				
				byte[][] data = message.getAllData();
				byte[] messagePart1 = data[1];
				byte[] messagePart2 = null;
				if (data.length > 2) {
					messagePart2 = data[2];
				}
				
				// Return the request but do not reply to the client now. This will be done by the Request.			
				return new Request(name, id, serverUrl, serverPort, messagePart1, messagePart2);
			}
			else if (type == Messages.CANCEL) {
				canceled = true;

				// Reply immediately.
				reply = responseToRequest();
				reply.send(responder);
				
				if (reply != null) {
					reply.destroy();
				}
				
				return null;
			}
		}
		finally {
			if (message != null) {
				message.destroy();
			}	
		}
			
		return null;
	}
	
	public void reply(byte[] part1, byte[] part2) {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(part1);
		message.add(part2);
		
		message.send(responder);
	}
	
	public void cancel() {
		Endpoint endpoint = This.getEndpoint().withPort(responderPort);

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CANCEL);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = This.getCom().createRequestSocket(endpoint.toString(), "zzzZZZ");
		requestSocket.requestJSON(request);
		
		// Terminate the socket.
		requestSocket.terminate();
	}

	private Zmq.Msg responseToRequest() {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(Messages.createRequestResponse(0, "OK")));
		
		return message;
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