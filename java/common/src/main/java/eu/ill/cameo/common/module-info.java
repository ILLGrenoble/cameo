module fr.ill.ics.cameo.common {
	
	requires transitive json.simple; // Automatic module name from file
	
	exports fr.ill.ics.cameo.common;
	exports fr.ill.ics.cameo.common.messages;
	exports fr.ill.ics.cameo.common.strings;
}