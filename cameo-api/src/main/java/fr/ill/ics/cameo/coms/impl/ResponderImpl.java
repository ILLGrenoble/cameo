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

import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.base.impl.RequestSocket;
import fr.ill.ics.cameo.base.impl.ThisImpl;
import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

public class ResponderImpl {

	public static final String RESPONDER_PREFIX = "rep.";

	private ThisImpl application;
	private int responderPort;
	private String name;
	private Zmq.Socket responder;
	
	private boolean ended = false;
	private boolean canceled = false;
	private ResponderWaitingImpl waiting = new ResponderWaitingImpl(this);
	
	public ResponderImpl(ThisImpl application, int responderPort, String name) {
		this.application = application;
		this.responderPort = responderPort;
		this.name = name;

		// create a socket REP
		responder = this.application.getContext().createSocket(Zmq.REP);
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
			JSONObject request = application.parse(message);
			
			// Get the type.
			long type = JSON.getLong(request, Messages.TYPE);
			
			if (type == Messages.REQUEST) {

				String name = JSON.getString(request, Messages.Request.APPLICATION_NAME);
				int id = JSON.getInt(request, Messages.Request.APPLICATION_ID);
				String serverUrl = JSON.getString(request, Messages.Request.SERVER_URL);
				int serverPort = JSON.getInt(request, Messages.Request.SERVER_PORT);
				int requesterPort = JSON.getInt(request, Messages.Request.REQUESTER_PORT);
				
				List<byte[]> data = message.getAllData();
				
				byte[] message1 = data.get(1);
				
				// Create the request implementation.
				RequestImpl impl = new RequestImpl(application, 
						name, 
						id, 
						message1, 
						serverUrl,
						serverPort,
						requesterPort);
				
				// Set the optional message 2.
				if (data.size() > 2) {
					impl.setMessage2(data.get(2));
				}
				
				return impl;
			}
			else if (type == Messages.CANCEL) {
				canceled = true;
				
				return null;
			}
		}
		catch (ParseException e) {
			System.err.println("Cannot parse message");
		}
		finally {
			if (message != null) {
				message.destroy();
			}	

			// Send to the requester
			reply = new Zmq.Msg();
			reply.add("OK");
			reply.send(responder);
			
			if (reply != null) {
				reply.destroy();
			}
		}
			
		return null;
	}
	
	public void cancel() {
		Endpoint endpoint = application.getEndpoint().withPort(responderPort);

		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.CANCEL);
		
		Zmq.Msg message = new Zmq.Msg();
		message.add(Messages.serialize(request));
		
		// Create the request socket. We can create it here because it should be called only once.
		RequestSocket requestSocket = application.createRequestSocket(endpoint.toString());
		requestSocket.request(message);
		
		// Terminate the socket.
		requestSocket.terminate();
	}

	public boolean isEnded() {
		return ended;
	}
	
	public boolean isCanceled() {
		return canceled;
	}
	
	public void terminate() {

		waiting.remove();
		this.application.getContext().destroySocket(responder);
		
		try {
			application.removePort(RESPONDER_PREFIX + name);
			
		} catch (Exception e) {
			System.err.println("Cannot terminate responder: " + e.getMessage());
		}
	}
	
	@Override
	public String toString() {
		return RESPONDER_PREFIX + name + ":" + application.getName() + "." + application.getId() + "@" + application.getEndpoint();
	}
	
}