package de.uhd.ifi.se.decision.management.jira.persistence.activeobjectpersistencemanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElementImpl;
import de.uhd.ifi.se.decision.management.jira.model.KnowledgeType;
import de.uhd.ifi.se.decision.management.jira.model.Link;
import de.uhd.ifi.se.decision.management.jira.model.LinkImpl;
import net.java.ao.test.jdbc.Data;
import net.java.ao.test.jdbc.NonTransactional;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;

@RunWith(ActiveObjectsJUnitRunner.class)
@Data(ActiveObjectPersistenceManagerTestSetUp.AoSentenceTestDatabaseUpdater.class)
public class TestDeleteLink extends ActiveObjectPersistenceManagerTestSetUp {

	private DecisionKnowledgeElement linkedDecisision;

	@Before
	public void setUp() {
		initialisation();
		DecisionKnowledgeElement element = new DecisionKnowledgeElementImpl();
		element.setProject("TEST");
		element.setType(KnowledgeType.SOLUTION);

		linkedDecisision = new DecisionKnowledgeElementImpl();
		linkedDecisision.setProject("TEST");
		linkedDecisision.setType(KnowledgeType.DECISION);

		DecisionKnowledgeElement elementWithDatabaseId = aoStrategy.insertDecisionKnowledgeElement(element, user);
		DecisionKnowledgeElement linkedDecisionWithDatabaseId = aoStrategy
				.insertDecisionKnowledgeElement(linkedDecisision, user);
		Link link = new LinkImpl(linkedDecisionWithDatabaseId, elementWithDatabaseId);
		aoStrategy.insertLink(link, user);
	}

	@Test(expected = NullPointerException.class)
	@NonTransactional
	public void testLinkNullUserNull() {
		aoStrategy.deleteLink(null, null);
	}

	@Test(expected = NullPointerException.class)
	@NonTransactional
	public void testLinkNullUserFilled() {
		aoStrategy.deleteLink(null, user);
	}

	@Test
	@NonTransactional
	public void testLinkFilledUserNull() {
		List<Link> links = aoStrategy.getLinks(linkedDecisision);
		Boolean bool = aoStrategy.deleteLink(links.get(0), user);
		assertTrue(bool);
	}

	@Test
	@NonTransactional
	public void testLinkFilledUserFilledLinkNotInTable() {
		Link emptyLink = new LinkImpl(linkedDecisision, linkedDecisision);
		assertFalse(aoStrategy.deleteLink(emptyLink, user));
	}

	@Test
	@NonTransactional
	public void testLinkFilledUserFilledLinkInTable() {
		List<Link> links = aoStrategy.getLinks(linkedDecisision);
		Boolean bool = aoStrategy.deleteLink(links.get(0), user);
		assertTrue(bool);
	}
}