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