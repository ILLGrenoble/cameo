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
 * Class defining a cancel event.
 */
public class CancelEvent extends Event {
	
	/**
	 * Constructor.
	 * @param id The application id.
	 * @param name The application name.
	 */
	public CancelEvent(int id, String name) {
		super(id, name);
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
		CancelEvent other = (CancelEvent) obj;
		
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
	
	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("type", "cancel");
		result.put("id", id);
		result.put("name", name);
		
		return result.toJSONString();
	}
	
}