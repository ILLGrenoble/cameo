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

package fr.ill.ics.cameo.base;

import jakarta.json.Json;
import jakarta.json.JsonObject;

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
		return Json.createObjectBuilder()
				.add("value", value)
				.add("message", message)
				.build()
				.toString();
	}
	
}