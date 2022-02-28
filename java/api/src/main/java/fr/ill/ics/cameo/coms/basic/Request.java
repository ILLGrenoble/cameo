package fr.ill.ics.cameo.coms.basic;

import java.util.List;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.base.ServerAndInstance;
import fr.ill.ics.cameo.messages.Messages;

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
	private String requesterServerEndpoint;
	private int timeout = 0;
		
	public Request(String requesterApplicationName, int requesterApplicationId, String serverUrl, int serverPort, byte[] messagePart1, byte[] messagePart2) {
		
		this.messagePart1 = messagePart1;
		this.messagePart2 = messagePart2;
		
		this.requesterApplicationName = requesterApplicationName;
		this.requesterApplicationId = requesterApplicationId;
		
		this.requesterServerEndpoint = serverUrl + ":" + serverPort;
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
	
	public ServerAndInstance connectToRequester(int options, boolean useProxy) {
		
		if (requesterServerEndpoint == null) {
			return null;
		}
		
		Server starterServer = new Server(requesterServerEndpoint, 0, useProxy);
		
		// Iterate the instances to find the id
		Instance starterInstance = null;
		List<Instance> instances = starterServer.connectAll(requesterApplicationName, options);
		for (Instance i : instances) {
			if (i.getId() == requesterApplicationId) {
				starterInstance = i;
				break;
			}
		}
		
		if (starterInstance == null) {
			return null;
		}
		
		return new ServerAndInstance(starterServer, starterInstance);
	}
	
	public ServerAndInstance connectToRequester(int options) {
		return connectToRequester(options, false);
	}
	
	public ServerAndInstance connectToRequester() {
		return connectToRequester(0, false);
	}

	@Override
	public String toString() {
		return "Request [id=" + requesterApplicationId + "]";
	}

	
	
}