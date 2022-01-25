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

package fr.ill.ics.cameo.coms.impl.zmq;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.base.This;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.coms.impl.RequesterImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class RequesterZmq implements RequesterImpl {

	private int requesterPort;
	private Zmq.Context context;
	private Zmq.Socket requester;
	private RequestSocket requestSocket;
	
	private boolean canceled = false;
		
	public void init(Endpoint endpoint, int responderPort) {
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();

		// Create the request socket.
		String responderEndpoint = endpoint.withPort(responderPort).toString();
		requestSocket = This.getCom().createRequestSocket(responderEndpoint);
		
		// Create the REP socket.
		requester = context.createSocket(Zmq.REP);
		
		String endpointPrefix = "tcp://*:";	
		
		// Loop to find an available port for the responder.
		while (true) {
		
			int port = This.getCom().requestPort();
			String reqEndpoint = endpointPrefix + port;

			try {
				requester.bind(reqEndpoint);
				requesterPort = port;
				break;
			}
			catch (Exception e) {
				This.getCom().setPortUnavailable(port);
			}
		}
	}
	
	public void send(byte[] requestData) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.REQUEST);
		request.put(Messages.Request.APPLICATION_NAME, This.getName());
		request.put(Messages.Request.APPLICATION_ID, This.getId());
		request.put(Messages.Request.SERVER_URL, This.getEndpoint().getProtocol() + "://" + This.getEndpoint().getAddress());
		request.put(Messages.Request.SERVER_PORT, This.getEndpoint().getPort());
		request.put(Messages.Request.REQUESTER_PORT, requesterPort);
		
		requestSocket.request(Messages.serialize(request), requestData);
	}
	
	public void send(String request) {
		send(Messages.serialize(request));
	}
	
	public void sendTwoParts(byte[] requestData1, byte[] requestData2) {
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.REQUEST);
		request.put(Messages.Request.APPLICATION_NAME, This.getName());
		request.put(Messages.Request.APPLICATION_ID, This.getId());
		request.put(Messages.Request.SERVER_URL, This.getEndpoint().getProtocol() + "://" + This.getEndpoint().getAddress());
		request.put(Messages.Request.SERVER_PORT, This.getEndpoint().getPort());
		request.put(Messages.Request.REQUESTER_PORT, requesterPort);
		
		requestSocket.request(Messages.serialize(request), requestData1, requestData2);
	}

	public byte[] receive() {
		
		Zmq.Msg message = null;
		
		try {
			message = Zmq.Msg.recvMsg(requester);

			if (message == null) {
				return null;
			}
			
			// Get the JSON request object.
			JSONObject request = This.getCom().parse(message.getFirstData());
			
			// Get the type.
			long type = JSON.getLong(request, Messages.TYPE);
						
			if (type == Messages.RESPONSE) {
				return message.getLastData();
			}
			else if (type == Messages.CANCEL) {
				canceled = true;
				return null;
			}
			else {
				return null;
			}
		}
		finally {
			if (message != null) {
				message.destroy();
			}
			
			// Send to the responder
			Zmq.Msg reply = responseToRequest();
			reply.send(requester);
		}
	}

	public String receiveString() {
		return Messages.parseString(receive());
	}
	
	public void cancel() {
		
		Endpoint endpoint = This.getEndpoint().withPort(requesterPort);

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CANCEL);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = This.getCom().createRequestSocket(endpoint.toString());
		requestSocket.requestJSON(request);
		
		// Terminate the socket.
		requestSocket.terminate();
	}

	public boolean isCanceled() {
		return canceled;
	}
	
	public void terminate() {
		
		context.destroySocket(requester);
		
		// Release the requester port.
		This.getCom().releasePort(requesterPort);
		
		// Terminate the request socket.
		requestSocket.terminate();
	}
	
	private Zmq.Msg responseToRequest() {
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(Messages.createRequestResponse(0, "OK")));
		
		return message;
	}
	
}