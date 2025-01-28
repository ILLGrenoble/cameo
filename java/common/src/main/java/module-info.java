/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

module eu.ill.cameo.common {
	
	requires transitive json.simple; // Automatic module name from file
	
	exports eu.ill.cameo.common;
	exports eu.ill.cameo.common.messages;
	exports eu.ill.cameo.common.strings;
}