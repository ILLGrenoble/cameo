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

package fr.ill.ics.cameo.coms;

import fr.ill.ics.cameo.base.Waiting;

/**
 * Class defining a waiting for the Requester class.
 */
public class RequesterWaiting extends Waiting {

	private Requester requester;

	/**
	 * Constructor.
	 * @param requester The requester.
	 */
	public RequesterWaiting(Requester requester) {
		this.requester = requester;
	}
	
	@Override
	public void cancel() {
		requester.cancel();
	}

	@Override
	public void terminate() {
		requester.terminate();
	}
	
}