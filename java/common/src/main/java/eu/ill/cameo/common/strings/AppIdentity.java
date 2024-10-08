package eu.ill.cameo.common.strings;

import org.json.simple.JSONObject;

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

	public JSONObject toJSON() {
						
		JSONObject result = new JSONObject();
		
		result.put("name", name);
		result.put("id", id);
		result.put("server", server.toJSON());
		
		return result;
	}
	
	public String toString() {
		return toJSON().toJSONString();
	}
	
}
