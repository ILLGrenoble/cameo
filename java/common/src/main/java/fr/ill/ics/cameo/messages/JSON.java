package fr.ill.ics.cameo.messages;

import java.io.StringReader;
import java.text.ParseException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;
import jakarta.json.stream.JsonParser.Event;

/**
 * Helper class to convert more easily JSONObject values.
 */
public class JSON {
	
	public static boolean hasKey(JsonObject object, String key) {
		Object value = object.get(key);
		return (value != null);
	}
	
	public static String getString(JsonObject object, String key) {
		return object.getString(key);
	}
	
	public static int getInt(JsonObject object, String key) {
		return object.getInt(key);
	}
	
	public static long getLong(JsonObject object, String key) {
		return object.getJsonNumber(key).longValue();
	}
	
	public static boolean getBoolean(JsonObject object, String key) {
		return object.getBoolean(key);
	}

	public static JsonObject getObject(JsonObject object, String key) {
		return object.getJsonObject(key);
	}
	
	public static JsonArray getArray(JsonObject object, String key) {
		return object.getJsonArray(key);
	}
	
	/**
	 * The JSON parser is not thread-safe and requires to synchronize the parse() method.
	 * The parser may be used by concurrent threads.
	 *
	 */
	public static class Parser {
		
		public synchronized JsonObject parse(String string) throws ParseException {
			
			JsonParser parser = Json.createParser(new StringReader(string));
			
			try {
				Event event = parser.next();
			
				if (event.equals(Event.START_OBJECT)) {
					return parser.getObject();
				}
			}
			catch (JsonParsingException e) {
				throw new ParseException(e.getMessage(), (int)e.getLocation().getStreamOffset());
			}
			finally {
				parser.close();
			}
			
			return null;
		}
	}
	
	/**
	 * Parses a string into a JSONObject.
	 * Method provided by convenience, a parser is created for each call.
	 * @param string
	 * @return
	 * @throws ParseException 
	 */
	public static JsonObject parse(String string) throws ParseException {
		return new Parser().parse(string);
	}

}
