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
 * Exception for a key that already exists.
 */
public class KeyAlreadyExistsException extends RemoteException {

	private static final long serialVersionUID = -3629600566663993697L;

	/**
	 * Constructor.
	 * @param message The message.
	 */
	public KeyAlreadyExistsException(String message) {
		super(message);
	}
}