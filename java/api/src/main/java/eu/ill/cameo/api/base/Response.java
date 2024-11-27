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
 * Class a response for Cameo server requests.
 */
public class Response {

	private int value;
	private String message;

	/**
	 * Constructor.
	 * @param value The value.
	 * @param message The message.
	 */
	public Response(int value, String message) {
		this.value = value;
		this.message = message;
	}
	
	/**
	 * Gets the value.
	 * @return The value.
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * Gets the message.
	 * @return The message.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Returns true if success.
	 * @return True if success.
	 */
	public boolean isSuccess() {
		return (value != -1);
	}

	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		
		result.put("value", value);
		result.put("message", message);
		
		return result.toJSONString();
	}
	
}