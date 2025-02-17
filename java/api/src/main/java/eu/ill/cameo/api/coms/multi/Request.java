/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.coms.multi;

import java.util.List;

import org.json.simple.JSONObject;

import eu.ill.cameo.api.base.App;
import eu.ill.cameo.api.base.ConnectionTimeout;
import eu.ill.cameo.api.base.Option;
import eu.ill.cameo.api.base.Server;
import eu.ill.cameo.api.base.ServerAndApp;
import eu.ill.cameo.common.messages.Messages;
import eu.ill.cameo.common.strings.AppIdentity;
import eu.ill.cameo.common.strings.Endpoint;
import eu.ill.cameo.common.strings.ServerIdentity;

/**
 * Class defining a request received by the multi responder.
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
	 * @param requesterApplicationId The request application id.
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
	 * @param timeout Timeout for the server initialization.
	 * @return The ServerAndApp object.
	 */
	public ServerAndApp connectToRequester(int options, int timeout) {
		
		if (requesterServerEndpoint == null) {
			return null;
		}
		
		Server starterServer = null;
		App starterInstance = null;
		
		boolean useProxy = ((options & Option.USE_PROXY) != 0);
		if (useProxy) {
			starterServer = Server.create(requesterServerEndpoint.withPort(requesterServerProxyPort), Option.USE_PROXY);
		}
		else {
			starterServer = Server.create(requesterServerEndpoint, 0);
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
	 * @return The ServerAndApp object.
	 */
	public ServerAndApp connectToRequester(int options) {
		return connectToRequester(options, 0);
	}

	/**
	 * Connects to the requester.
	 * @return The ServerAndApp object.
	 */
	public ServerAndApp connectToRequester() {
		return connectToRequester(0, 0);
	}

	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("type", "multi-request");
		result.put("app", new AppIdentity(requesterApplicationName, requesterApplicationId, new ServerIdentity(requesterServerEndpoint.toString(), false)).toJSON());
		
		return result.toJSONString();
	}

	
	
}