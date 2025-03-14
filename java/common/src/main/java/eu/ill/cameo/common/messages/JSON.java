/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

package eu.ill.cameo.common.messages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Helper class to convert more easily JSONObject values.
 */
public class JSON {
	
	public static boolean hasKey(JSONObject object, String key) {
		Object value = object.get(key);
		return (value != null);
	}
	
	public static String getString(JSONObject object, String key) {
		return (String)object.get(key);
	}
	
	public static int getInt(JSONObject object, String key) {
		return ((Long)object.get(key)).intValue();
	}
	
	public static long getLong(JSONObject object, String key) {
		return (Long)object.get(key);
	}
	
	public static boolean getBoolean(JSONObject object, String key) {
		return (Boolean)object.get(key);
	}

	public static JSONObject getObject(JSONObject object, String key) {
		return (JSONObject)object.get(key);
	}
	
	public static JSONArray getArray(JSONObject object, String key) {
		return (JSONArray)object.get(key);
	}
	
	/**
	 * The JSON parser is not thread-safe and requires to synchronize the parse() method.
	 * The parser may be used by concurrent threads.
	 *
	 */
	public static class Parser {
		
		private JSONParser parser = new JSONParser();
		
		public synchronized JSONObject parse(String string) throws ParseException {
			return (JSONObject)parser.parse(string);
		}
	}
	
	/**
	 * Parses a string into a JSONObject.
	 * Method provided by convenience, a parser is created for each call. Use a ConcurrentParser to obtain better performance.
	 * @param string
	 * @return
	 * @throws ParseException
	 */
	public static JSONObject parse(String string) throws ParseException {
		return (JSONObject)new JSONParser().parse(string);
	}

}