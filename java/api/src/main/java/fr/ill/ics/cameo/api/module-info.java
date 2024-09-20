module fr.ill.ics.cameo.api {
	
	requires fr.ill.ics.cameo.com;
	requires fr.ill.ics.cameo.processhandle;
	
	requires transitive fr.ill.ics.cameo.common;
		
	exports fr.ill.ics.cameo.api.base;
	
	exports fr.ill.ics.cameo.api.coms;
	exports fr.ill.ics.cameo.api.coms.basic;
	exports fr.ill.ics.cameo.api.coms.multi;
	
	exports fr.ill.ics.cameo.api.factory;
}