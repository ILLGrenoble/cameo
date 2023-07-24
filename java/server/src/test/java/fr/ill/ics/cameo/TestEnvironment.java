package fr.ill.ics.cameo;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Properties;

import org.junit.Test;

import fr.ill.ics.cameo.server.manager.Environment;

public class TestEnvironment {

	@Test
	public void testLoad() {
		
		Properties rawVariables = new Properties();
		
		rawVariables.put("VAR1", "/etc/file");
		rawVariables.put("VAR2", "dir/log");
		rawVariables.put("VAR3", "/usr/local/:$VAR1");
		rawVariables.put("VAR4", "$VAR1:/usr/local/");
		rawVariables.put("VAR5", "$VAR1:/usr/local/:$VAR2");
		rawVariables.put("VAR6", "$VAR1:/usr/local/:$VAR2:/usr/lib");
		rawVariables.put("VAR7", "/usr/local/:$HOME");
		
		HashMap<String, String> variables = Environment.loadVariables(rawVariables);
		
		assertEquals(variables.size(), rawVariables.size());
		assertEquals(variables.get("VAR1"), "/etc/file");
		assertEquals(variables.get("VAR2"), "dir/log");
		assertEquals(variables.get("VAR3"), "/usr/local/:/etc/file");
		assertEquals(variables.get("VAR4"), "/etc/file:/usr/local/");
		assertEquals(variables.get("VAR5"), "/etc/file:/usr/local/:dir/log");
		assertEquals(variables.get("VAR6"), "/etc/file:/usr/local/:dir/log:/usr/lib");
		assertEquals(variables.get("VAR7"), "/usr/local/:");
	}
}
