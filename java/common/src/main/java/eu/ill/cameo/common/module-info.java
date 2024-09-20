module eu.ill.cameo.common {
	
	requires transitive json.simple; // Automatic module name from file
	
	exports eu.ill.cameo.common;
	exports eu.ill.cameo.common.messages;
	exports eu.ill.cameo.common.strings;
}