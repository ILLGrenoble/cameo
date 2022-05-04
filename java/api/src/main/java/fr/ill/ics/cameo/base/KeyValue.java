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


/**
 * Class defining a key value.
 */
public class KeyValue {

	/**
	 * Type of the status.
	 */
	public enum Status {UNDEFINED, STORED, REMOVED};

	private Status status = Status.UNDEFINED;
	private String key;
	private String value;
	
	/**
	 * Constructor.
	 * @param key The key.
	 */
	public KeyValue(String key) {
		this.key = key;
	}
	
	/**
	 * Sets the status.
	 * @param status The status.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	
	/**
	 * Sets the value.
	 * @param value The value.
	 */
	public void setValue(String value) {
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
		KeyValue other = (KeyValue) obj;
		
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
	
}