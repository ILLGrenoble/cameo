/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.api.base;

import org.json.simple.JSONObject;

/**
 * Class defining a key event.
 */
public class KeyEvent extends Event {
	
	/**
	 * Type of the status.
	 */
	public enum Status {STORED, REMOVED};

	private Status status;
	private String key;
	private String value;

	/**
	 * Constructor.
	 * @param id The application id.
	 * @param name The application name.
	 * @param status The status of the key.
	 * @param key The key.
	 * @param value The value.
	 */
	public KeyEvent(int id, String name, Status status, String key, String value) {
		super(id, name);
		this.status = status;
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Gets the status.
	 * @return The status.
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Gets the key.
	 * @return The key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the value.
	 * @return The value.
	 */
	public String getValue() {
		return value;
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
		KeyEvent other = (KeyEvent) obj;
		
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
		if (key != other.key) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		}
		else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("type", "key");
		result.put("id", id);
		result.put("name", name);
		result.put("key", key);
		result.put("status", status);
		result.put("value", value);
		
		return result.toJSONString();
	}

	
}