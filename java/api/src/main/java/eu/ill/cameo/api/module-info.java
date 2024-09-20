module eu.ill.cameo.api {
	
	requires eu.ill.cameo.com;
	requires eu.ill.cameo.processhandle;
	
	requires transitive eu.ill.cameo.common;
		
	exports eu.ill.cameo.api.base;
	
	exports eu.ill.cameo.api.coms;
	exports eu.ill.cameo.api.coms.basic;
	exports eu.ill.cameo.api.coms.multi;
	
	exports eu.ill.cameo.api.factory;
}