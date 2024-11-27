/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.api.base;

/**
 * Interface defining an abstract Cameo object that can be initialized and terminated.
 */
public abstract class StateObject {
	
	private enum InternalState {INIT, READY, TERMINATED}
	private InternalState state;
	
	/**
	 * Initializes the object.
	 */
	public abstract void init();
	
	/**
	 * Returns true if is ready.
	 * @return True if is ready.
	 */
	public boolean isReady() {
		return state == InternalState.READY;
	}

	/**
	 * Terminates the object.
	 */
	public abstract void terminate();
	
	/**
	 * Returns true if is terminated.
	 * @return True if is terminated.
	 */
	public boolean isTerminated() {
		return state == InternalState.TERMINATED;
	}
	
	/**
	 * Sets the state ready.
	 */
	protected void setReady() {
		state = InternalState.READY;
	}
	
	/**
	 * Sets the state terminated.
	 */
	protected void setTerminated() {
		state = InternalState.TERMINATED;
	}
}