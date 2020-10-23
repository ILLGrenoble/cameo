package fr.ill.ics.cameo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.ill.ics.cameo.strings.Endpoint;

public class TestStrings {

	@Test
	public void testEndpoint() {
		
		assertEquals("gamma75:9999", new Endpoint("gamma75", 9999).toString());
		
		Endpoint endpoint = Endpoint.parse("gamma75:9999");
		
		assertEquals("gamma75", endpoint.getAddress());
		assertEquals(9999, endpoint.getPort());

		boolean error = false;
		try {
			Endpoint.parse("gamma:75:9999");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);
		
		error = false;
		try {
			Endpoint.parse("gamma75::9999");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);
	}
}
