package fr.ill.ics.cameo.strings;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class AppIdentity {

	private String name;
	private int id;
	private ServerIdentity server;
			
	public AppIdentity(String name, int id, ServerIdentity server) {
		super();
		this.name = name;
		this.id = id;
		this.server = server;
	}

	public JsonObject toJSON() {
						
		JsonObject object = Json.createObjectBuilder()
			.add("name", name)
			.add("id", id)
			.add("server", server.toJSON())
			.build();
		
		return object;
	}
	
	public String toString() {
		return toJSON().toString();
	}
	
}
