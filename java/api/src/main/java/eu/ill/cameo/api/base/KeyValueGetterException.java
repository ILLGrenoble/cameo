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
 * Class defining an exception when getting a key value fails.
 */
public class KeyValueGetterException extends RemoteException {

	private static final long serialVersionUID = 8288957337240690562L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public KeyValueGetterException(String message) {
		super(message);
	}
}