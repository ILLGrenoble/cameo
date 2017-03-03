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

import org.zeromq.ZContext;
import org.zeromq.ZMsg;

import com.google.protobuf.ByteString;

import fr.ill.ics.cameo.proto.Messages.MessageType.Type;

public class RequestImpl {

	private ApplicationImpl application;
	ZContext context;
	private String requesterEndpoint;
	private ByteString message;
	private ByteString message2;
	private String requesterApplicationName;
	private int requesterApplicationId;
	private String requesterServerEndpoint;
	
	public RequestImpl(ApplicationImpl application, ZContext context, String requesterApplicationName, int requesterApplicationId, ByteString message, String serverUrl, int serverPort, int requesterPort) {
		
		this.application = application;
		this.context = context;
		this.requesterEndpoint = serverUrl + ":" + requesterPort;
		this.message = message;
		
		this.requesterApplicationName = requesterApplicationName;
		this.requesterApplicationId = requesterApplicationId;
		
		this.requesterServerEndpoint = serverUrl + ":" + serverPort;
	}
	
	public void setMessage2(ByteString message2) {
		this.message2 = message2;
	}
	
	public ByteString get() {
		return message;
	}
	
	public ByteString get2() {
		return message2;
	}

	public void reply(byte[] response) {
		
		ZMsg responseMessage = application.createRequest(Type.RESPONSE);
		responseMessage.add(response);
		
		application.tryRequest(responseMessage, requesterEndpoint);
	}
	
	public void reply(String response) {
		reply(Buffer.serialize(response));
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