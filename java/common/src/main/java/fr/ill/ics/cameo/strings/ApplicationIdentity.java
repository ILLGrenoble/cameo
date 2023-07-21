package fr.ill.ics.cameo.strings;

import fr.ill.ics.cameo.messages.Messages;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class ApplicationIdentity {

	private String name;
	private Integer id;
	private Endpoint endpoint;
			
	public ApplicationIdentity(String name, Integer id, Endpoint endpoint) {
		super();
		this.name = name;
		this.id = id;
		this.endpoint = endpoint;
	}
	
	public ApplicationIdentity(String name, Endpoint endpoint) {
		super();
		this.name = name;
		this.endpoint = endpoint;
	}	
	
	public String getName() {
		return name;
	}
	
	public Integer getId() {
		return id;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public JsonObjectBuilder toJSONBuilder() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add(Messages.ApplicationIdentity.NAME, name);
		if (id != null) {
			builder.add(Messages.ApplicationIdentity.ID, id);
		}
		builder.add(Messages.ApplicationIdentity.SERVER, endpoint.toString());
		
		return builder;
	}
	
	public JsonObject toJSON() {
		return toJSONBuilder().build();
	}
	
}
