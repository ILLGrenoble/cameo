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

public class PublisherWaiting extends Waiting {

	private Publisher publisher;
	
	public PublisherWaiting(Publisher publisher) {
		this.publisher = publisher;
	}
	
	@Override
	public void cancel() {
		publisher.cancelWaitForSubscribers();
	}

	@Override
	public void terminate() {
		publisher.terminate();
	}
	
}