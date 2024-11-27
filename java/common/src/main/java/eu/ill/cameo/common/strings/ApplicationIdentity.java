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
		
		result.put(Messages.ApplicationIdentity.NAME, name);
		if (id != null) {
			result.put(Messages.ApplicationIdentity.ID, id);
		}
		result.put(Messages.ApplicationIdentity.SERVER, endpoint.toString());
		
		return result;
	}
	
}