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

package eu.ill.cameo.api.coms;

import eu.ill.cameo.api.base.Waiting;

/**
 * Class defining a waiting for the Publisher class.
 *
 */
public class PublisherWaiting extends Waiting {

	private Publisher publisher;

	/**
	 * Constructor.
	 * @param publisher The publisher.
	 */
	public PublisherWaiting(Publisher publisher) {
		this.publisher = publisher;
	}
	
	@Override
	public void cancel() {
		publisher.cancel();
	}

	@Override
	public void terminate() {
		publisher.terminate();
	}
	
}