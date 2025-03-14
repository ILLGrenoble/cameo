/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.base;

import org.json.simple.JSONObject;

/**
 * Class defining a result event.
 */
public class ResultEvent extends Event {
	
	private byte[] data;
	
	/**
	 * Constructor.
	 * @param id The application id.
	 * @param name The application name.
	 * @param data The result data.
	 */
	public ResultEvent(int id, String name, byte[] data) {
		super(id, name);
		this.data = data;
	}
		
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResultEvent other = (ResultEvent) obj;
		
		if (id != other.id) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public byte[] getData() {
		return data;
	}
	
	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("type", "result");
		result.put("id", id);
		result.put("name", name);
		
		return result.toJSONString();
	}
	
}