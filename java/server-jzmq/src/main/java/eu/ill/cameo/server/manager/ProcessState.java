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
 * describe states of process
 * @author heurtin
 *
 */
public enum ProcessState {
	RUNNING, UNKNOWN, ZOMBIE, DEAD
}