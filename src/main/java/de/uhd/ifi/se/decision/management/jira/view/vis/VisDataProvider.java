package de.uhd.ifi.se.decision.management.jira.view.vis;

import java.util.List;

import com.atlassian.jira.user.ApplicationUser;

import de.uhd.ifi.se.decision.management.jira.filtering.FilterExtractor;
import de.uhd.ifi.se.decision.management.jira.filtering.FilterSettings;
import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.persistence.AbstractPersistenceManager;

public class VisDataProvider {

	private String projectKey;
	private ApplicationUser user;
	private VisGraph graph;
	private VisTimeLine timeLine;
	private FilterExtractor filterExtractor;
	private List<DecisionKnowledgeElement> decisionKnowledgeElements;

	// Evolution Views
	public VisDataProvider(ApplicationUser user, FilterSettings filterSettings) {
		if (user == null || filterSettings == null) {
			return;
		}
		this.projectKey = filterSettings.getProjectKey();
		this.user = user;
		FilterExtractor filterExtractor = new FilterExtractor(this.user, filterSettings);
		List<DecisionKnowledgeElement> decisionKnowledgeElements = filterExtractor
				.getAllElementsMatchingCompareFilter();
		graph = new VisGraph(decisionKnowledgeElements, projectKey);
		this.timeLine = new VisTimeLine(decisionKnowledgeElements);
	}

	// JQL Filter and JIRA Filter
	public VisDataProvider(String projectKey, String elementKey, String query, ApplicationUser user) {
		this.projectKey = projectKey;
		this.user = user;
		this.filterExtractor = new FilterExtractor(projectKey, user, query);
		decisionKnowledgeElements = filterExtractor.getAllElementsMatchingQuery();
		AbstractPersistenceManager persistenceManager = AbstractPersistenceManager
				.getDefaultPersistenceStrategy(projectKey);
		DecisionKnowledgeElement rootElement = persistenceManager.getDecisionKnowledgeElement(elementKey);
		graph = new VisGraph(rootElement, decisionKnowledgeElements);
	}

	// Filter Issue Module
	public VisDataProvider(String elementKey, ApplicationUser user, FilterSettings filterSettings) {
		this.projectKey = filterSettings.getProjectKey();
		this.user = user;
		this.filterExtractor = new FilterExtractor(user, filterSettings);
		AbstractPersistenceManager persistenceManager = AbstractPersistenceManager
				.getDefaultPersistenceStrategy(projectKey);
		DecisionKnowledgeElement rootElement = persistenceManager.getDecisionKnowledgeElement(elementKey);
		decisionKnowledgeElements = filterExtractor.getAllElementsMatchingCompareFilter();
		graph = new VisGraph(rootElement, decisionKnowledgeElements);
	}

	public VisGraph getVisGraph() {
		return this.graph;
	}

	public VisTimeLine getTimeLine() {
		if (timeLine == null) {
			this.timeLine = new VisTimeLine(projectKey);
		}
		return this.timeLine;
	}
}