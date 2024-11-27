/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.common.strings;

import org.json.simple.JSONObject;

import eu.ill.cameo.common.messages.Messages;

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
	
	public JSONObject toJSON() {
		
		JSONObject result = application.toJSON();
		
		if (starter != null) {
			result.put(Messages.ApplicationIdentity.STARTER, starter.toJSON());
			result.put(Messages.ApplicationIdentity.STARTER_PROXY_PORT, starterProxyPort);
			result.put(Messages.ApplicationIdentity.STARTER_LINKED, starterLinked);
		}
		
		return result;
	}
	
	public String toJSONString() {
		return toJSON().toJSONString();
	}

}