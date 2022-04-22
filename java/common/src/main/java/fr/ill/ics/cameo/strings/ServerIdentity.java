package fr.ill.ics.cameo.strings;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.messages.Messages;

public class ServerIdentity {

	private String endpoint;
	private boolean proxy;
			
	public ServerIdentity(String endpoint, boolean proxy) {
		super();
		this.endpoint = endpoint;
		this.proxy = proxy;
	}

	public JSONObject toJSON() {
		
		JSONObject result = new JSONObject();
		
		result.put("endpoint", endpoint);
		result.put("proxy", proxy);
		
		return result;
	}
	
	public String toString() {
		return toJSON().toJSONString();
	}
	
}
