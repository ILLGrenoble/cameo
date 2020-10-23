package fr.ill.ics.cameo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import fr.ill.ics.cameo.strings.ApplicationAndStarterIdentities;
import fr.ill.ics.cameo.strings.ApplicationIdentity;
import fr.ill.ics.cameo.strings.Endpoint;
import fr.ill.ics.cameo.strings.Name;
import fr.ill.ics.cameo.strings.NameId;

public class TestStrings {

	@Test
	public void testEndpoint() {
		
		assertEquals("tcp://gamma75:9999", new Endpoint("gamma75", 9999).toString());
		
		Endpoint endpoint = Endpoint.parse("tcp://gamma75:9999");
		
		assertEquals("gamma75", endpoint.getAddress());
		assertEquals(9999, endpoint.getPort());

		boolean error = false;
		try {
			Endpoint.parse("tc://gamma75:9999");
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
	public void testName() {
		
		assertTrue(Name.check("myapp"));
		assertTrue(Name.check("MyApp"));
		assertTrue(Name.check("MyApp0"));
		assertTrue(Name.check("My-App0"));
		assertTrue(Name.check("My_App0"));
		
		assertFalse(Name.check("myapp!"));
		assertFalse(Name.check("my app"));
	}
	
	@Test
	public void testNameId() {
		
		assertEquals("my-app.31", new NameId("my-app", 31).toString());
		
		NameId nameId = NameId.parse("my-app.31");
		
		assertEquals("my-app", nameId.getName());
		assertEquals(Integer.valueOf(31), nameId.getId());

		nameId = NameId.parse("my-app32");
		assertEquals(null, nameId.getId());
		
		boolean error = false;
		try {
			NameId.parse("my-app.ff");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);
	}
	
	@Test
	public void testApplicationIdentity() {
		
		assertEquals("my-app.31@tcp://gamma75:9999", new ApplicationIdentity(new NameId("my-app", 31), new Endpoint("gamma75", 9999)).toString());
		assertEquals("my-app@tcp://gamma75:9999", new ApplicationIdentity(new NameId("my-app", null), new Endpoint("gamma75", 9999)).toString());
		
		ApplicationIdentity identity = ApplicationIdentity.parse("my-app.31@tcp://gamma75:9999");
		
		assertEquals("my-app", identity.getNameId().getName());
		assertEquals(Integer.valueOf(31), identity.getNameId().getId());
		assertEquals("gamma75", identity.getEndpoint().getAddress());
		assertEquals(9999, identity.getEndpoint().getPort());
		
		identity = ApplicationIdentity.parse("my-app.31@tcp://127.65.198.1:9999");
		
		assertEquals("my-app", identity.getNameId().getName());
		assertEquals(Integer.valueOf(31), identity.getNameId().getId());
		assertEquals("127.65.198.1", identity.getEndpoint().getAddress());
		assertEquals(9999, identity.getEndpoint().getPort());
		
		identity = ApplicationIdentity.parse("my-app@tcp://gamma75:9999");
		
		assertEquals("my-app", identity.getNameId().getName());
		assertEquals(null, identity.getNameId().getId());
		assertEquals("gamma75", identity.getEndpoint().getAddress());
		assertEquals(9999, identity.getEndpoint().getPort());
		
		boolean error = false;
		try {
			ApplicationIdentity.parse("my-app.ff@tcp://gamma75:9999");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);
		
		error = false;
		try {
			ApplicationIdentity.parse("my-app.ff@tcp:/gamma75:9999");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);
		
		error = false;
		try {
			ApplicationIdentity.parse("my-app.ff@tcp://gamma75:99G");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);
	}
	
	@Test
	public void testApplicationAndStarterIdentities() {
		
		ApplicationAndStarterIdentities identities = ApplicationAndStarterIdentities.parse("my-app.31@tcp://gamma75:9999:your-app.15@tcp://gamma63:789");
		
		assertEquals("my-app.31@tcp://gamma75:9999", identities.getApplication().toString());
		assertEquals("your-app.15@tcp://gamma63:789", identities.getStarter().toString());
		
		identities = ApplicationAndStarterIdentities.parse("my-app.31@tcp://gamma75:9999:");
		
		assertEquals("my-app.31@tcp://gamma75:9999", identities.getApplication().toString());
		assertEquals(null, identities.getStarter());
		
		boolean error = false;
		try {
			ApplicationAndStarterIdentities.parse("my-app.31@tcp://gamma75:9999");
		}
		catch (Exception e) {
			error = true;
		}
		assertTrue(error);		
	}
}
