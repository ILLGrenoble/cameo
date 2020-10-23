package fr.ill.ics.cameo.strings;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.messages.Message;

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

	public JSONObject toJSON() {
		
		JSONObject result = new JSONObject();
		
		result.put(Message.ApplicationIdentity.NAME, name);
		if (id != null) {
			result.put(Message.ApplicationIdentity.ID, id);
		}
		result.put(Message.ApplicationIdentity.SERVER, endpoint.toString());
		
		return result;
	}
	
}
