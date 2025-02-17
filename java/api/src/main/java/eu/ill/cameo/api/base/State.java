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

import java.util.ArrayList;

/**
 * Class owning the different states of a Cameo applications. 
 *
 */
public class State {
	
	/**
	 * Unknown state.
	 */
	public final static int NIL = 0;
	
	/**
	 * Starting state.
	 */
	public final static int STARTING = 1;
	
	/**
	 * Running state.
	 */
	public final static int RUNNING = 2;
	
	/**
	 * Stopping state.
	 */
	public final static int STOPPING = 4;
	
	/**
	 * Killing state.
	 */
	public final static int KILLING = 8;
	
	/**
	 * Processing error state.
	 */
	public final static int PROCESSING_FAILURE = 16;
	
	/**
	 * Failure state.
	 */
	public final static int FAILURE = 32;
	
	/**
	 * Success state.
	 */
	public final static int SUCCESS = 64;
	
	/**
	 * Stopped state.
	 */
	public final static int STOPPED = 128;
	
	/**
	 * Killed state.
	 */
	public final static int KILLED = 256;
	
	/**
	 * Parses the state string.
	 * @param value The state string.
	 * @return The state.
	 */
	public static int parse(String value) {
		
		if (value.equals("NIL")) {
			return State.NIL;
		}
		else if (value.equals("STARTING")) {
			return State.STARTING;
		}
		else if (value.equals("RUNNING")) {
			return State.RUNNING;
		}
		else if (value.equals("STOPPING")) {
			return State.STOPPING;
		}
		else if (value.equals("KILLING")) {
			return State.KILLING;
		}
		else if (value.equals("PROCESSING_FAILURE")) {
			return State.PROCESSING_FAILURE;
		}
		else if (value.equals("FAILURE")) {
			return State.FAILURE;
		}
		else if (value.equals("SUCCESS")) {
			return State.SUCCESS;
		}
		else if (value.equals("STOPPED")) {
			return State.STOPPED;
		}
		else if (value.equals("KILLED")) {
			return State.KILLED;
		}
		
		return State.NIL;
	}
			
	/**
	 * Converts the list of application states into a string.
	 * @param applicationStates The list of states.
	 * @return The string concatenation of the states.
	 */
	public static String toString(int applicationStates) {
		
		ArrayList<String> states = new ArrayList<String>();
		
		if ((applicationStates & State.STARTING) != 0) {
			states.add("STARTING");
		}
		
		if ((applicationStates & State.RUNNING) != 0) {
			states.add("RUNNING");
		}
		
		if ((applicationStates & State.STOPPING) != 0) {
			states.add("STOPPING");
		}
		
		if ((applicationStates & State.KILLING) != 0) {
			states.add("KILLING");
		}
		
		if ((applicationStates & State.PROCESSING_FAILURE) != 0) {
			states.add("PROCESSING_FAILURE");
		}
		
		if ((applicationStates & State.FAILURE) != 0) {
			states.add("FAILURE");
		}
		
		if ((applicationStates & State.SUCCESS) != 0) {
			states.add("SUCCESS");
		}
		
		if ((applicationStates & State.STOPPED) != 0) {
			states.add("STOPPED");
		}
		
		if ((applicationStates & State.KILLED) != 0) {
			states.add("KILLED");
		}
		
		if (states.size() == 0) {
			return "NIL";
		}
		
		if (states.size() == 1) {
			return states.get(0);
		}
		
		String result = "";			
		
		for (int i = 0; i < states.size() - 1; i++) {
			result += states.get(i) + ", ";
		}
		result += states.get(states.size() - 1);
		
		return result;
	}
}