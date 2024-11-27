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