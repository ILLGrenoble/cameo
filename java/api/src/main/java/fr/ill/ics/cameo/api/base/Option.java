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

package fr.ill.ics.cameo.api.base;

/**
 * Class defining the options of the Server.connect() and Server.start() methods.
 */
public class Option {
	
	/**
	 * Constant for none.
	 */
	public final static int NONE = 0;
	
	/**
	 * Constant for outputstream.
	 */
	public final static int OUTPUTSTREAM = 1;
	
	/**
	 * Constant for unlinked.
	 */
	public final static int UNLINKED = 2;
}