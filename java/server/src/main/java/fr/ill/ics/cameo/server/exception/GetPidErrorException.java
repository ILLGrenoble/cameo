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

package fr.ill.ics.cameo.server.exception;

public class GetPidErrorException extends Exception {
	
	private static final long serialVersionUID = -5139958494856857849L;
	private Exception exception;
	
	public GetPidErrorException(Exception exception) {
		this.exception = exception;
	}
	
	public Exception getException() {
		return exception;
	}
	
	public GetPidErrorException() {
		super();
	}
}