package fr.ill.ics.cameo.coms.basic;

import java.util.List;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.base.Instance;
import fr.ill.ics.cameo.base.Server;
import fr.ill.ics.cameo.messages.Messages;

/**
 * Class Request.
 * 
 */
public class Request {
	
	private Responder responder = null;
	private Server requesterServer = null;
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
		
		JSONObject request = new JSONObject();
		request.put(Messages.TYPE, Messages.RESPONSE);

		responder.reply(request, response);
		
		return true;
	}
	
	public boolean reply(String response) {
		return reply(Messages.serialize(response));
	}
	
	public Instance connectToRequester() {
		
		// Instantiate the requester server if it is null.
		if (requesterServer == null) {
			requesterServer = new Server(requesterServerEndpoint, timeout);
		}	
		
		// Connect and find the instance.
		List<Instance> instances = requesterServer.connectAll(requesterApplicationName);
		
		for (Instance instance : instances) {
			if (instance.getId() == requesterApplicationId) {
				return instance;
			}
		}
		
		// Not found.
		return null;
	}
	
	/**
	 * Gets the requester server and transfers the ownership. The client code is responsible to terminate the server.
	 * @return
	 */
	public Server getServer() {
		
		// Transfers the ownership of the server.
		Server result = requesterServer;
		requesterServer = null;
		
		return result;
	}
	
	public void terminate() {
		
		if (requesterServer != null) {
			requesterServer.terminate();
		}
	}

	@Override
	public String toString() {
		return "Request [id=" + requesterApplicationId + "]";
	}

	
	
}