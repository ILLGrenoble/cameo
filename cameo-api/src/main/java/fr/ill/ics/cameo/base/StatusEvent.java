package fr.ill.ics.cameo.base;
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



public class StatusEvent extends Event {
	
	private int applicationState;
	private int pastApplicationStates;
	private Integer exitCode;
	
	public final static StatusEvent SYNC = new StatusEvent(-1, "", Application.State.UNKNOWN, Application.State.UNKNOWN);
	public final static StatusEvent END = new StatusEvent(-2, "", Application.State.UNKNOWN, Application.State.UNKNOWN);
	
	public StatusEvent(int id, String name, int applicationState, int pastApplicationStates) {
		super(id, name);
		this.applicationState = applicationState;
		this.pastApplicationStates = pastApplicationStates;
	}
	
	public StatusEvent(int id, String name, int applicationState, int pastApplicationStates, int exitCode) {
		super(id, name);
		this.applicationState = applicationState;
		this.pastApplicationStates = pastApplicationStates;
		this.exitCode = exitCode;
	}
	
	public int getState() {
		return applicationState;
	}
	
	public int getPastStates() {
		return pastApplicationStates;
	}
	
	/**
	 * Gets the exit code.
	 * @return null if the exit code is not defined.
	 */
	public Integer getExitCode() {
		return exitCode;
	}

	@Override
	public String toString() {
		return "ApplicationStatus [id=" + id + ", applicationState=" + Application.State.toString(applicationState) + ", name=" + name + "]";
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
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
}