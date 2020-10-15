package fr.ill.ics.cameo;
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



public class KeyEvent extends Event {
	
	public enum Status {STORED, REMOVED};

	private Status status;
	private String key;
	private String value;
	
	public KeyEvent(int id, String name, Status status, String key, String value) {
		super(id, name);
		this.status = status;
		this.key = key;
		this.value = value;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public String getKey() {
		return key;
	}

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
		} else if (!name.equals(other.name)) {
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
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
	
}