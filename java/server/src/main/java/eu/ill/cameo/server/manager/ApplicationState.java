/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.server.manager;


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
	public final static int PROCESSING_FAILURE = 16;
	public final static int FAILURE = 32;
	public final static int SUCCESS = 64;
	public final static int STOPPED = 128;
	public final static int KILLED = 256;
}