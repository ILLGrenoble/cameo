package fr.ill.ics.cameo.strings;

import fr.ill.ics.cameo.messages.Messages;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class ApplicationWithStarterIdentity {

	private ApplicationIdentity application;
	private ApplicationIdentity starter;
	private int starterProxyPort;
	private boolean starterLinked;
	
	public ApplicationWithStarterIdentity(ApplicationIdentity application, ApplicationIdentity starter, int starterProxyPort, boolean starterLinked) {
		this.application = application;
		this.starter = starter;
		this.starterProxyPort = starterProxyPort;
		this.starterLinked = starterLinked;
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
	
	public JsonObject toJSON() {
		
		JsonObjectBuilder builder = application.toJSONBuilder();
		
		if (starter != null) {
			builder.add(Messages.ApplicationIdentity.STARTER, starter.toJSON());
			builder.add(Messages.ApplicationIdentity.STARTER_PROXY_PORT, starterProxyPort);
			builder.add(Messages.ApplicationIdentity.STARTER_LINKED, starterLinked);
		}
		
		return builder.build();
	}
	
	public String toJSONString() {
		return toJSON().toString();
	}

}
