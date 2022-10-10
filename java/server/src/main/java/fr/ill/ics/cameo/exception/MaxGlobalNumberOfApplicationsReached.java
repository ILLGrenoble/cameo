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

package fr.ill.ics.cameo.exception;

public class MaxGlobalNumberOfApplicationsReached extends Exception {

	private static final long serialVersionUID = -564543760445565040L;
	private String name;
	
	public MaxGlobalNumberOfApplicationsReached(String name) {
		this.name = name;
	}

	public String getMessage() {
		return "Cannot start '" + name + "' because the maximum global number of running applications is reached";
	}
}