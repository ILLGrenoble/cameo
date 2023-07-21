package fr.ill.ics.cameo.strings;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class ServerIdentity {

	private String endpoint;
	private boolean proxy;
			
	public ServerIdentity(String endpoint, boolean proxy) {
		super();
		this.endpoint = endpoint;
		this.proxy = proxy;
	}

	public JsonObject toJSON() {
		
		JsonObject object = Json.createObjectBuilder()
			.add("endpoint", endpoint)
			.add("proxy", proxy)
			.build();
		
		return object;
	}
	
	public String toString() {
		return toJSON().toString();
	}
	
}
