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

package fr.ill.ics.cameo.api.coms.basic;

import java.util.List;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.api.base.App;
import fr.ill.ics.cameo.api.base.ConnectionTimeout;
import fr.ill.ics.cameo.api.base.Server;
import fr.ill.ics.cameo.api.base.ServerAndApp;
import fr.ill.ics.cameo.common.messages.Messages;
import fr.ill.ics.cameo.common.strings.AppIdentity;
import fr.ill.ics.cameo.common.strings.Endpoint;
import fr.ill.ics.cameo.common.strings.ServerIdentity;

/**
 * Class defining a request for the basic responder.
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
	
	/**
	 * Constructor.
	 * @param requesterApplicationName The requester application name.
	 * @param requesterApplicationId The requester application id.
	 * @param serverEndpoint The server endpoint.
	 * @param serverProxyPort The server proxy port.
	 * @param messagePart1 The message part 1.
	 * @param messagePart2 The message part 2.
	 */
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

	/**
	 * Gets the message.
	 * @return The binary message.
	 */
	public byte[] get() {
		return messagePart1;
	}
	
	/**
	 * Gets the message in string.
	 * @return The stringified message.
	 */
	public String getString() {
		return Messages.parseString(messagePart1);
	}
	
	/**
	 * Gets the two binary message parts.
	 * @return The two binary parts message. The array has size 2.
	 */
	public byte[][] getTwoParts() {
		
		byte[][] result = new byte[2][];
		result[0] = messagePart1;
		result[1] = messagePart2;
		
		return result;
	}
	
	/**
	 * Replies to the requester.
	 * @param response The response.
	 */
	public void reply(byte[] response) {
		
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Messages.TYPE, Messages.RESPONSE);

		responder.reply(jsonRequest, response);
	}
	
	/**
	 * Replies a string to the requester.
	 * @param response The string response.
	 */
	public void replyString(String response) {
		reply(Messages.serialize(response));
	}
	
	/**
	 * Connects to the requester.
	 * @param options Options of connection.
	 * @param useProxy True if proxy is used.
	 * @param timeout Timeout for the server used for the initialization and subsequent requests.
	 * @return The ServerAndApp object.
	 */
	public ServerAndApp connectToRequester(int options, boolean useProxy, int timeout) {
		
		if (requesterServerEndpoint == null) {
			return null;
		}
		
		Server starterServer = null;
		App starterInstance = null;
		
		if (useProxy) {
			starterServer = Server.create(requesterServerEndpoint.withPort(requesterServerProxyPort), true);
		}
		else {
			starterServer = Server.create(requesterServerEndpoint, false);
		}
		
		starterServer.setTimeout(timeout);
		
		try {
			starterServer.init();
			
			// Iterate the instances to find the id
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
		}
		catch (ConnectionTimeout e) {
			// Timeout while initializing the server.
		}	
		
		return new ServerAndApp(starterServer, starterInstance);
	}
	
	/**
	 * Connects to the requester.
	 * @param options Options of connection.
	 * @param useProxy True if proxy is used.
	 * @return The ServerAndApp object.
	 */
	public ServerAndApp connectToRequester(int options, boolean useProxy) {
		return connectToRequester(options, useProxy, 0);
	}
	
	/**
	 * Connects to the requester.
	 * @param options Options of connection.
	 * @return The ServerAndApp object.
	 */
	public ServerAndApp connectToRequester(int options) {
		return connectToRequester(options, false, 0);
	}

	/**
	 * Connects to the requester.
	 * @return The ServerAndApp object.
	 */
	public ServerAndApp connectToRequester() {
		return connectToRequester(0, false, 0);
	}

	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("type", "basic-request");
		result.put("app", new AppIdentity(requesterApplicationName, requesterApplicationId, new ServerIdentity(requesterServerEndpoint.toString(), false)).toJSON());
		
		return result.toJSONString();
	}

	
	
}