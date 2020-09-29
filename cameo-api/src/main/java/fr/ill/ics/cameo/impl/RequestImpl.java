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

package fr.ill.ics.cameo.impl;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.Zmq;
import fr.ill.ics.cameo.messages.Message;

public class RequestImpl {

	private ThisImpl application;
	private String requesterEndpoint;
	private byte[] message;
	private byte[] message2;
	private String requesterApplicationName;
	private int requesterApplicationId;
	private String requesterServerEndpoint;
	
	public RequestImpl(ThisImpl application, String requesterApplicationName, int requesterApplicationId, byte[] message, String serverUrl, int serverPort, int requesterPort) {
		
		this.application = application;
		this.requesterEndpoint = serverUrl + ":" + requesterPort;
		this.message = message;
		
		this.requesterApplicationName = requesterApplicationName;
		this.requesterApplicationId = requesterApplicationId;
		
		this.requesterServerEndpoint = serverUrl + ":" + serverPort;
	}
	
	public void setMessage2(byte[] message2) {
		this.message2 = message2;
	}
	
	public byte[] get() {
		return message;
	}
	
	public byte[] get2() {
		return message2;
	}

	public String getString() {
		return Message.parseString(message);
	}
	
	public void reply(byte[] response) {
		
		JSONObject request = new JSONObject();
		request.put(Message.TYPE, Message.RESPONSE);
		
		Zmq.Msg message = application.message(request);
		
		// Set request in the next frame.
		message.add(response);

		// Create a new socket.
		// Notice that trying to reuse a socket by calling connect() does not work (it is worse with jeromq)
		RequestSocket requestSocket = application.createRequestSocket(requesterEndpoint);
		requestSocket.request(message);
		requestSocket.terminate();
	}
	
	public void reply(String response) {
		reply(Message.serialize(response));
	}
	
	public String getRequesterApplicationName() {
		return requesterApplicationName;
	}

	public int getRequesterApplicationId() {
		return requesterApplicationId;
	}
	
	public String getRequesterServerEndpoint() {
		return requesterServerEndpoint;
	}

	@Override
	public String toString() {
		return "Request [endpoint=" + requesterEndpoint + ", id=" + requesterApplicationId + "]";
	}

}