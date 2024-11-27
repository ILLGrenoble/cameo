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
 * Class defining a filtered event listener.
 */
public class FilteredEventListener {

	private EventListener listener;
	private boolean filtered;

	/**
	 * Constructor.
	 * @param listener The listener.
	 * @param filtered True if filtered.
	 */
	public FilteredEventListener(EventListener listener, boolean filtered) {
		this.listener = listener;
		this.filtered = filtered;
	}

	/**
	 * Returns the listener.
	 * @return The listener.
	 */
	public EventListener getListener() {
		return listener;
	}

	/**
	 * Returns true if is filtered.
	 * @return True if is filtered.
	 */
	public boolean isFiltered() {
		return filtered;
	}
	
	
}