package de.uhd.ifi.se.decision.management.jira.model.link;

import com.atlassian.jira.issue.link.IssueLink;
import de.uhd.ifi.se.decision.management.jira.TestSetUp;
import de.uhd.ifi.se.decision.management.jira.mocks.MockIssueLink;
import de.uhd.ifi.se.decision.management.jira.model.KnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.model.Link;
import de.uhd.ifi.se.decision.management.jira.model.LinkType;
import de.uhd.ifi.se.decision.management.jira.persistence.tables.LinkInDatabase;
import de.uhd.ifi.se.decision.management.jira.testdata.Links;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Test class for links between decision knowledge elements
 */
public class TestLink extends TestSetUp {

	public Link link;

	@Before
	public void setUp() {
		init();
		link = Links.getTestLinks().get(0);
	}

	@Test
	public void testGetType() {
		assertEquals(LinkType.RELATE.toString(), link.getType().toLowerCase());
	}

	@Test
	public void testSetType() {
		link.setType(LinkType.RELATE.toString() + "New");
		assertEquals("The LinkType has to be in the Enum LinkType. Otherwise the default \"RELATE\" LinkType is used.", LinkType.RELATE.toString(), link.getType());
		link.setType(LinkType.RELATE);
	}

	@Test
	public void testGetIdOfSourceElement() {
		assertEquals(2, link.getSource().getId());
	}

	@Test
	public void testGetIdOfDestinationElement() {
		assertEquals(4, link.getTarget().getId());
	}

	@Test
	public void testSetSourceElementById() {
		link.setSourceElement(2, "i");
		assertEquals(2, link.getSource().getId(), 0.0);
	}

	@Test
	public void testSetDestinationElementById() {
		long oldId = link.getTarget().getId();
		link.setDestinationElement(5, "i");
		assertEquals(5, link.getTarget().getId(), 0.0);
		link.setDestinationElement(oldId, "i");
	}

	@Test
	public void testGetSourceElement() {
		assertEquals("TEST-2", link.getSource().getKey());
	}

	@Test
	public void testSetSourceElement() {
		KnowledgeElement element = new KnowledgeElement();
		KnowledgeElement oldElement = link.getSource();
		element.setKey("TestNew");
		link.setSourceElement(element);
		assertEquals(element.getKey(), link.getSource().getKey());
		link.setSourceElement(oldElement);
	}

	@Test
	public void testGetDestinationElement() {
		assertEquals("TEST-4", link.getTarget().getKey());
	}

	@Test
	public void testSetDestinationElement() {
		KnowledgeElement element = new KnowledgeElement();
		KnowledgeElement oldElement = link.getTarget();
		element.setKey("TestNew");
		link.setDestinationElement(element);
		assertEquals(element.getKey(), link.getTarget().getKey());
		link.setDestinationElement(oldElement);
	}

	@Test
	public void testConstructorLinkEntity() {
		LinkInDatabase linkInDatabase = mock(LinkInDatabase.class);
		Link link = new Link(linkInDatabase);
		assertNotNull(link);
	}

	@Test
	public void testConstructorDecisionKnowledgeElement() {
		KnowledgeElement sourceElement = new KnowledgeElement();
		sourceElement.setId(14);
		sourceElement.setKey("TestSourceElement");
		KnowledgeElement destinationElement = new KnowledgeElement();
		destinationElement.setId(15);
		destinationElement.setKey("TestDestinationElement");
		Link link = new Link(sourceElement, destinationElement);
		assertEquals("TestSourceElement", link.getSource().getKey());
	}

	@Test
	public void testConstructorIssueLink() {
		IssueLink issueLink = new MockIssueLink(1, 2, 1);
		Link link = new Link(issueLink);
		assertNotNull(link);
	}

	@Test
	public void testSetIdOfSourceElement() {
		long oldId = link.getSource().getId();
		link.setIdOfSourceElement(231);
		assertEquals(231, link.getSource().getId());
		link.setIdOfSourceElement(oldId);
	}

	@Test
	public void testSetIdOfDestinationElement() {
		long oldId = link.getTarget().getId();
		link.setIdOfDestinationElement(231);
		assertEquals(231, link.getTarget().getId());
		link.setIdOfDestinationElement(oldId);
	}

	@Test
	public void testGetSource() {
		assertEquals(2, link.getSource().getId());
	}

	@Test
	public void testGetTarget() {
		assertEquals(4, link.getTarget().getId());
	}

	@Test
	public void testGetWeight() {
		assertEquals(1.0, link.getWeight(), 0.0);
	}

	@Test
	public void testLinkIdZeroElementSame() {
		Link newLink = new Link(link.getSource(), link.getTarget());
		long linkId = newLink.getId();
		newLink.setId(0);
		assertEquals(linkId, newLink.getId());
	}

	@Test
	public void testContainsUnknownDocumentationLocation() {
		assertFalse(link.containsUnknownDocumentationLocation());
	}
}