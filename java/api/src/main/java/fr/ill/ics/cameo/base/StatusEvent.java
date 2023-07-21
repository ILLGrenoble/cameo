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
 * Class defining a status event.
 */
public class StatusEvent extends Event {
	
	private int applicationState;
	private int pastApplicationStates;
	private Integer exitCode;
	
	/**
	 * Sync event.
	 */
	public final static StatusEvent SYNC = new StatusEvent(-1, "", State.NIL, State.NIL);
	
	/**
	 * End event.
	 */
	public final static StatusEvent END = new StatusEvent(-2, "", State.NIL, State.NIL);
	
	/**
	 * Constructor.
	 * @param id The application id.
	 * @param name The application name.
	 * @param state The current state.
	 * @param pastStates The past states.
	 */
	public StatusEvent(int id, String name, int state, int pastStates) {
		super(id, name);
		this.applicationState = state;
		this.pastApplicationStates = pastStates;
	}
	
	/**
	 * Constructor.
	 * @param id The application id.
	 * @param name The application name.
	 * @param state The current state.
	 * @param pastStates The past states.
	 * @param exitCode The exit code.
	 */
	public StatusEvent(int id, String name, int applicationState, int pastApplicationStates, int exitCode) {
		super(id, name);
		this.applicationState = applicationState;
		this.pastApplicationStates = pastApplicationStates;
		this.exitCode = exitCode;
	}
	
	/**
	 * Gets the current state.
	 * @return The state.
	 */
	public int getState() {
		return applicationState;
	}
	
	/**
	 * Gets the past states.
	 * @return The past states.
	 */
	public int getPastStates() {
		return pastApplicationStates;
	}
	
	/**
	 * Gets the exit code.
	 * @return The exit code, null if the exit code is not defined.
	 */
	public Integer getExitCode() {
		return exitCode;
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
		StatusEvent other = (StatusEvent) obj;
		
		if (applicationState != other.applicationState) {
			return false;
		}
		if (pastApplicationStates != other.pastApplicationStates) {
			return false;
		}
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
		return Json.createObjectBuilder()
				.add("type", "status")
				.add("id", id)
				.add("name", name)
				.add("state", State.toString(applicationState))
				.add("exit_code", exitCode)
				.build()
				.toString();
	}
	
}