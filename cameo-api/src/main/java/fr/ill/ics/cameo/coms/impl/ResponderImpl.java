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

package fr.ill.ics.cameo.coms.impl;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.Application.This;
import fr.ill.ics.cameo.base.RequestSocket;
import fr.ill.ics.cameo.base.impl.zmq.ContextZmq;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class ResponderImpl {

	public static final String RESPONDER_PREFIX = "rep.";

	private int responderPort;
	private String name;
	
	private Zmq.Context context;
	private Zmq.Socket responder;
	
	private boolean ended = false;
	private boolean canceled = false;
	private ResponderWaitingImpl waiting = new ResponderWaitingImpl(this);
	
	public ResponderImpl(int responderPort, String name) {
		this.responderPort = responderPort;
		this.name = name;
		this.context = ((ContextZmq)This.getCom().getContext()).getContext();

		// create a socket REP
		responder = context.createSocket(Zmq.REP);
		responder.bind("tcp://*:" + responderPort);
		
		waiting.add();
	}
	
	public String getName() {
		return name;
	}

	public RequestImpl receive() {
		
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
				int requesterPort = JSON.getInt(request, Messages.Request.REQUESTER_PORT);
				
				byte[][] data = message.getAllData();
				
				byte[] message1 = data[1];
				
				// Create the request implementation.
				RequestImpl impl = new RequestImpl(name, 
						id, 
						message1, 
						serverUrl,
						serverPort,
						requesterPort);
				
				// Set the optional message 2.
				if (data.length > 2) {
					impl.setMessage2(data[2]);
				}
				
				return impl;
			}
			else if (type == Messages.CANCEL) {
				canceled = true;
				
				return null;
			}
		}
		finally {
			if (message != null) {
				message.destroy();
			}	

			// Send to the requester
			reply = responseToRequest();
			reply.send(responder);
			
			if (reply != null) {
				reply.destroy();
			}
		}
			
		return null;
	}
	
	public void cancel() {
		Endpoint endpoint = This.getEndpoint().withPort(responderPort);

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CANCEL);
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = This.getCom().createRequestSocket(endpoint.toString());
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

		waiting.remove();
		context.destroySocket(responder);
		
		try {
			This.getCom().removePort(RESPONDER_PREFIX + name);
			
		} catch (Exception e) {
			System.err.println("Cannot terminate responder: " + e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return RESPONDER_PREFIX + name + ":" + This.getName() + "." + This.getId() + "@" + This.getEndpoint();
	}
	
}