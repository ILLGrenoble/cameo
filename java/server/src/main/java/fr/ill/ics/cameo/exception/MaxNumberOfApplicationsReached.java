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

public class MaxNumberOfApplicationsReached extends Exception {

	private static final long serialVersionUID = -679186322182419615L;
	private String name;
	
	public MaxNumberOfApplicationsReached(String name) {
		this.name = name;
	}

	public String getMessage() {
		return "Cannot start '" + name + "' because the maximum number of applications is reached";
	}
}