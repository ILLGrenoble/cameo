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

package eu.ill.cameo.api.base;

/**
 * Base class for events.
 */
public class Event {
	
	protected int id;
	protected String name;
	
	/**
	 * Constructor.
	 * @param id The application id.
	 * @param name The application name.
	 */
	public Event(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	/**
	 * Gets the application id.
	 * @return The application id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the application name.
	 * @return The application name.
	 */	
	public String getName() {
		return name;
	}
}