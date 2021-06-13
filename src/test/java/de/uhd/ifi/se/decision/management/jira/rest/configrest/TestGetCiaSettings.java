package de.uhd.ifi.se.decision.management.jira.rest.configrest;

import de.uhd.ifi.se.decision.management.jira.TestSetUp;
import de.uhd.ifi.se.decision.management.jira.rest.ConfigRest;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class TestGetCiaSettings extends TestSetUp {

	protected ConfigRest configRest;

	@Before
	public void setUp() {
		init();
		configRest = new ConfigRest();
	}

	@Test
	public void testProjectKeyNull() {
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), configRest.getCiaSettings(null).getStatus());
	}

	@Test
	public void testProjectKeyEmpty() {
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), configRest.getCiaSettings("").getStatus());
	}

	@Test
	public void testProjectKeyInvalid() {
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), configRest.getCiaSettings("InvalidKey").getStatus());
	}

	@Test
	public void testProjectKeyValid() {
		assertEquals(Response.Status.OK.getStatusCode(), configRest.getCiaSettings("TEST").getStatus());
	}
}