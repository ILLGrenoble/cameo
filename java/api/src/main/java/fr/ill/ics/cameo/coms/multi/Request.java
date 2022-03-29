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

package fr.ill.ics.cameo.coms.multi;

import java.util.List;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.App;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.ServerAndApp;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.Endpoint;

/**
 * Class Request.
 * 
 */
public class Request {
	
	private Responder responder = null;
	private byte[] messagePart1;
	private byte[] messagePart2;
	private String requesterApplicationName;
	private int requesterApplicationId;
	private Endpoint requesterServerEndpoint;
	private int requesterServerProxyPort;
	private int timeout = 0;
		
	public Request(String requesterApplicationName, int requesterApplicationId, String serverEndpoint, int serverProxyPort, byte[] messagePart1, byte[] messagePart2) {
		
		this.messagePart1 = messagePart1;
		this.messagePart2 = messagePart2;
		
		this.requesterApplicationName = requesterApplicationName;
		this.requesterApplicationId = requesterApplicationId;
		
		this.requesterServerEndpoint = Endpoint.parse(serverEndpoint);
		this.requesterServerProxyPort = serverProxyPort;
	}
	
	void setResponder(Responder responder) {
		this.responder = responder;
	}
	
	public byte[] getBinary() {
		return messagePart1;
	}
	
	public String get() {
		return Messages.parseString(messagePart1);
	}
	
	public byte[][] getTwoBinaryParts() {
		
		byte[][] result = new byte[2][];
		result[0] = messagePart1;
		result[1] = messagePart2;
		
		return result;
	}
	
	public void setTimeout(int value) {
		timeout = value;
	}
	
	public boolean reply(byte[] response) {
		
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Messages.RESPONSE);

		responder.reply(jsonRequest, response);
		
		return true;
	}
	
	public boolean reply(String response) {
		return reply(Messages.serialize(response));
	}
	
	public ServerAndApp connectToRequester(int options, boolean useProxy) {
		
		if (requesterServerEndpoint == null) {
			return null;
		}
		
		Server starterServer;
		
		if (useProxy) {
			starterServer = new Server(requesterServerEndpoint.withPort(requesterServerProxyPort), 0, true);
		}
		else {
			starterServer = new Server(requesterServerEndpoint, 0, false);	
		}
		
		// Iterate the instances to find the id
		App starterInstance = null;
		List<App> instances = starterServer.connectAll(requesterApplicationName, options);
		for (App i : instances) {
			if (i.getId() == requesterApplicationId) {
				starterInstance = i;
				break;
			}
		}
		
		if (starterInstance == null) {
			return null;
		}
		
		return new ServerAndApp(starterServer, starterInstance);
	}
	
	public ServerAndApp connectToRequester(int options) {
		return connectToRequester(options, false);
	}
	
	public ServerAndApp connectToRequester() {
		return connectToRequester(0, false);
	}

	@Override
	public String toString() {
		return "Request [id=" + requesterApplicationId + "]";
	}

	
	
}