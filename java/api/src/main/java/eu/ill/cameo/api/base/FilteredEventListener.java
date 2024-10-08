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
