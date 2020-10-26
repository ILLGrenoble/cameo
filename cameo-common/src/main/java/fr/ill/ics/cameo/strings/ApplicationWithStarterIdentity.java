package fr.ill.ics.cameo.strings;

import org.json.simple.JSONObject;

import fr.ill.ics.cameo.messages.Message;

public class ApplicationWithStarterIdentity {

	private ApplicationIdentity application;
	private ApplicationIdentity starter;
	
	public ApplicationWithStarterIdentity(ApplicationIdentity application, ApplicationIdentity starter) {
		this.application = application;
		this.starter = starter;
	}
	
	public ApplicationWithStarterIdentity(ApplicationIdentity application) {
		this.application = application;
	}

	public ApplicationIdentity getApplication() {
		return application;
	}

	public ApplicationIdentity getStarter() {
		return starter;
	}
	
	public JSONObject toJSON() {
		
		JSONObject result = application.toJSON();
		
		if (starter != null) {
			result.put(Message.ApplicationIdentity.STARTER, starter.toJSON());
		}
		
		return result;
	}
	
	public String toJSONString() {
		return toJSON().toJSONString();
	}

}
