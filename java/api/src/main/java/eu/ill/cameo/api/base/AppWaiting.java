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
 * Class defining a waiting for an App.
 *
 */
public class AppWaiting extends Waiting {

	private App instance;

	/**
	 * Constructor.
	 * @param app The app.
	 */
	public AppWaiting(App app) {
		this.instance = app;
	}
	
	@Override
	public void cancel() {
		instance.cancel();
	}

	@Override
	public void terminate() {
		instance.terminate();		
	}
	
}