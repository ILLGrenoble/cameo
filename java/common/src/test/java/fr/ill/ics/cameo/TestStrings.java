package fr.ill.ics.cameo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import fr.ill.ics.cameo.messages.JSON;
import fr.ill.ics.cameo.messages.Messages;
import fr.ill.ics.cameo.strings.ApplicationIdentity;
import fr.ill.ics.cameo.strings.ApplicationWithStarterIdentity;
import fr.ill.ics.cameo.strings.Endpoint;

public class TestStrings {

	@Test
	public void testEndpoint() {
		
		assertEquals("tcp://gamma75:9999", new Endpoint("gamma75", 9999).toString());
		assertEquals("tcp://gamma75:9999", new Endpoint("tcp", "gamma75", 9999).toString());
		assertEquals("ws://gamma75:9999", new Endpoint("ws", "gamma75", 9999).toString());
		
		Endpoint endpoint = Endpoint.parse("tcp://gamma75:9999");
		
		assertEquals("tcp", endpoint.getProtocol());
		assertEquals("gamma75", endpoint.getAddress());
		assertEquals(9999, endpoint.getPort());

		Endpoint endpoint2 = new Endpoint("tcp", "gamma75", 9999);
		assertEquals(endpoint, endpoint2);
		
		boolean error = false;
		try {
			Endpoint.parse("gamma75:9999");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);
		
		error = false;
		try {
			Endpoint.parse("tcp://gamma75::9999");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);
	}
	
	@Test
	public void testApplicationIdentity() {
		
		ApplicationIdentity identity = new ApplicationIdentity("my-app", 31, new Endpoint("gamma75", 9000));
		
		String jsonString = identity.toJSON().toJSONString();
		
		try {
			JSONObject jsonIdentity = JSON.parse(jsonString);
			
			assertEquals("my-app", JSON.getString(jsonIdentity, Messages.ApplicationIdentity.NAME));
			assertEquals(31, JSON.getInt(jsonIdentity, Messages.ApplicationIdentity.ID));
			assertEquals("tcp://gamma75:9000", JSON.getString(jsonIdentity, Messages.ApplicationIdentity.SERVER));
		}
		catch (ParseException e) {
			assertTrue(false);	
		}
	}
	
	@Test
	public void testApplicationWithStarterIdentity() {
		
		ApplicationIdentity application = new ApplicationIdentity("my-app", 31, new Endpoint("gamma75", 9000));
		ApplicationIdentity starter = new ApplicationIdentity("your-app", 76, new Endpoint("gamma57", 7000));
		
		ApplicationWithStarterIdentity identity = new ApplicationWithStarterIdentity(application, starter, 10000, true);
		
		String jsonString = identity.toJSON().toJSONString();
		
		try {
			JSONObject jsonApplication = JSON.parse(jsonString);
			
			assertEquals("my-app", JSON.getString(jsonApplication, Messages.ApplicationIdentity.NAME));
			assertEquals(31, JSON.getInt(jsonApplication, Messages.ApplicationIdentity.ID));
			assertEquals("tcp://gamma75:9000", JSON.getString(jsonApplication, Messages.ApplicationIdentity.SERVER));
			
			JSONObject jsonStarter = JSON.getObject(jsonApplication, Messages.ApplicationIdentity.STARTER);
			
			assertEquals("your-app", JSON.getString(jsonStarter, Messages.ApplicationIdentity.NAME));
			assertEquals(76, JSON.getInt(jsonStarter, Messages.ApplicationIdentity.ID));
			assertEquals("tcp://gamma57:7000", JSON.getString(jsonStarter, Messages.ApplicationIdentity.SERVER));
		}
		catch (ParseException e) {
			assertTrue(false);	
		}
		
		ApplicationWithStarterIdentity identity2 = new ApplicationWithStarterIdentity(application);
		
		jsonString = identity2.toJSON().toJSONString();
		
		try {
			JSONObject jsonApplication = JSON.parse(jsonString);

			assertFalse(jsonApplication.containsKey(Messages.ApplicationIdentity.STARTER));
		}
		catch (ParseException e) {
			assertTrue(false);	
		}
	}
}
