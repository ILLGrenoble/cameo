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

package fr.ill.ics.cameo.manager;


/**
 * List of possible application states.
 *
 */
public class ApplicationState {
	
	public final static int NIL = 0;
	public final static int STARTING = 1;
	public final static int RUNNING = 2;
	public final static int STOPPING = 4;
	public final static int KILLING = 8;
	public final static int PROCESSING_ERROR = 16;
	public final static int FAILURE = 32;
	public final static int SUCCESS = 64;
	public final static int STOPPED = 128;
	public final static int KILLED = 256;
}