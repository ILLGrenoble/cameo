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
	public final static int UNLINKED = (1 << 1);
	
	/**
	 * Constant for no proxy.
	 */
	public final static int USE_PROXY = (1 << 2);
}