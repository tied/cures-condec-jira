package de.uhd.ifi.se.decision.management.jira.persistence.activeobjectstrategy;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import de.uhd.ifi.se.decision.management.jira.extraction.persistence.DecisionKnowledgeInCommentEntity;
import de.uhd.ifi.se.decision.management.jira.extraction.persistence.LinkBetweenDifferentEntitiesEntity;
import de.uhd.ifi.se.decision.management.jira.persistence.DecisionKnowledgeElementEntity;
import net.java.ao.test.jdbc.DatabaseUpdater;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;

import de.uhd.ifi.se.decision.management.jira.TestComponentGetter;
import de.uhd.ifi.se.decision.management.jira.mocks.MockDefaultUserManager;
import de.uhd.ifi.se.decision.management.jira.mocks.MockTransactionTemplate;
import de.uhd.ifi.se.decision.management.jira.persistence.ActiveObjectStrategy;
import net.java.ao.EntityManager;

public class ActiveObjectStrategyTestSetUp {
	
	protected EntityManager entityManager;
	protected ApplicationUser user;
	protected ActiveObjectStrategy aoStrategy;

	public void initialisation() {
		ActiveObjects activeObjects = new TestActiveObjects(entityManager);
		TestComponentGetter.init(activeObjects, new MockTransactionTemplate(), new MockDefaultUserManager());
		aoStrategy = new ActiveObjectStrategy("TEST");
		user = new MockApplicationUser("NoFails");
	}

	public static final class AoSentenceTestDatabaseUpdater implements DatabaseUpdater {
		@SuppressWarnings("unchecked")
		@Override
		public void update(EntityManager entityManager) throws Exception {
			entityManager.migrate(DecisionKnowledgeElementEntity.class);
			entityManager.migrate(DecisionKnowledgeInCommentEntity.class);
			entityManager.migrate(LinkBetweenDifferentEntitiesEntity.class);
		}
	}
}
