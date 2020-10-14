package fr.ill.ics.cameo.messages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Helper class to convert more easily JSONObject values.
 */
public class JSON {

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
	
	public static JSONArray getArray(JSONObject object, String key) {
		return (JSONArray)object.get(key);
	}
	
	/**
	 * The JSON parser is not thread-safe and requires to synchronize the parse() method.
	 * The parser may be used by concurrent threads.
	 *
	 */
	public static class ConcurrentParser {
		
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
