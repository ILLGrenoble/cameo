package fr.ill.ics.cameo.manager;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

public class Environment {

	public static String replaceVariables(String rawValue, HashMap<String, String> variables) {
		
		String result = "";
		int lastIndex = 0;
		
		while (true) {
			
			int startIndex = rawValue.indexOf('$', lastIndex);
			if (startIndex == -1) {
				result += rawValue.substring(lastIndex);
				break;
			}
			
			String variableName;
			int endIndex = rawValue.indexOf(':', startIndex + 1);
			if (endIndex == -1) {
				endIndex = rawValue.length();
				variableName = rawValue.substring(startIndex + 1);
				
				result += rawValue.substring(lastIndex, startIndex);
				
				String variableValue = variables.get(variableName);
				result += (variableValue == null ? "" : variableValue);
				
				break;
			}
			else {
				variableName = rawValue.substring(startIndex + 1, endIndex);
				
				result += rawValue.substring(lastIndex, startIndex);
				
				String variableValue = variables.get(variableName);
				result += (variableValue == null ? "" : variableValue) + ":";
			}
			
			lastIndex = endIndex + 1;
		}
		
		return result;
	}
	
	public static HashMap<String, String> loadVariables(Properties rawVariables) {
	
		HashMap<String, String> variables = new HashMap<String, String>();
		
		// Copying the content to the hash map.
		for (Entry<Object, Object> e : rawVariables.entrySet()) {
			
			if (e.getKey() instanceof String && e.getValue() instanceof String) {
				
				String variableValue = (String) e.getValue();
				variables.put((String) e.getKey(), replaceVariables(variableValue, variables));
			}
		}
		
		return variables;
	}
}
