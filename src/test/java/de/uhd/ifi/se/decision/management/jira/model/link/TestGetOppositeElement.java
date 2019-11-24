package de.uhd.ifi.se.decision.management.jira.model.link;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uhd.ifi.se.decision.management.jira.TestSetUp;
import de.uhd.ifi.se.decision.management.jira.model.Link;
import de.uhd.ifi.se.decision.management.jira.testdata.Links;

public class TestGetOppositeElement extends TestSetUp {

	public Link link;

	@Before
	public void setUp() {
		init();
		link = Links.getTestLinks().get(0);
	}

	@Test
	public void testValidLink() {
		assertEquals(link.getSource(), link.getOppositeElement(link.getTarget()));
		assertEquals(link.getTarget(), link.getOppositeElement(link.getSource()));
	}
}