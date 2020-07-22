package de.uhd.ifi.se.decision.management.jira.consistency.contextinformation;

import de.uhd.ifi.se.decision.management.jira.consistency.suggestions.LinkSuggestion;
import de.uhd.ifi.se.decision.management.jira.model.KnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.model.Link;
import de.uhd.ifi.se.decision.management.jira.persistence.ConfigPersistenceManager;
import de.uhd.ifi.se.decision.management.jira.persistence.ConsistencyPersistenceHelper;
import de.uhd.ifi.se.decision.management.jira.persistence.KnowledgePersistenceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ContextInformation implements ContextInformationProvider {
	private KnowledgeElement element;
	private List<ContextInformationProvider> cips;
	private Map<String, LinkSuggestion> linkSuggestions;


	public ContextInformation(KnowledgeElement element) {
		this.element = element;
		// Add context information providers
		this.cips = new ArrayList<>();
		this.cips.add(new TextualSimilarityCIP());
		this.cips.add(new TracingCIP());
		this.cips.add(new TimeCIP());
		this.cips.add(new UserCIP());

	}


	public Collection<KnowledgeElement> getLinkedKnowledgeElements() {
		Set<KnowledgeElement> linkedKnowledgeElements = new HashSet<>();
		List<Link> linkCollection = this.element.getLinks();
		if (linkCollection != null) {
			for (Link link : linkCollection) {
				linkedKnowledgeElements.add(link.getSource());
				linkedKnowledgeElements.add(link.getTarget());

			}
		}
		return linkedKnowledgeElements;
	}


	public Collection<LinkSuggestion> getLinkSuggestions() {
		//Add all issues of project to projectKnowledgeElements set
		Set<KnowledgeElement> projectKnowledgeElements = new HashSet<>(
			KnowledgePersistenceManager
				.getOrCreate(element.getProject().getProjectKey())
				.getKnowledgeElements()
		);

		projectKnowledgeElements.remove(this.element);
		this.assessRelation(element, new ArrayList<>(projectKnowledgeElements));
		//calculate context score

		//get filtered issues
		Set<KnowledgeElement> elementsToKeep = this.filterKnowledgeElements(projectKnowledgeElements);

		//retain scores of filtered issues
		return this.linkSuggestions.values()
			.parallelStream()
			// issue was not filtered out
			.filter(linkSuggestion -> elementsToKeep.contains(linkSuggestion.getTargetElement()))
			// the score is higher or equal to the minimum probability set by the admin for the project
			.filter(linkSuggestion -> linkSuggestion.getTotalScore() >= ConfigPersistenceManager.getMinLinkSuggestionScore(this.element.getProject().getProjectKey()))
			.collect(Collectors.toCollection(ArrayList::new));
	}

	private Set<KnowledgeElement> filterKnowledgeElements(Set<KnowledgeElement> projectKnowledgeElements) {
		//Create union of all issues to be filtered out.
		Set<KnowledgeElement> filteredKnowledgeElements = new HashSet<>(projectKnowledgeElements);
		Set<KnowledgeElement> filterOutElements = new HashSet<>(this.getLinkedKnowledgeElements());
		filterOutElements.addAll(ConsistencyPersistenceHelper
			.getDiscardedLinkSuggestions(this.element));
		filterOutElements.add(this.element);

		//Calculate difference between all issues of project and the issues that need to be filtered out.
		filteredKnowledgeElements.removeAll(filterOutElements);

		return filteredKnowledgeElements;
	}

	@Override
	public void assessRelation(KnowledgeElement baseElement, List<KnowledgeElement> knowledgeElements) {
		// init the link suggestions
		this.linkSuggestions = new ConcurrentHashMap<>();
		for (KnowledgeElement otherElement : knowledgeElements) {
			linkSuggestions.put(otherElement.getKey(), new LinkSuggestion(this.element, otherElement));
		}

		this.cips.parallelStream().forEach((cip) -> {
			cip.assessRelation(this.element, new ArrayList<>(knowledgeElements));

			double nullCompensation = 0.;

			Collection<LinkSuggestion> suggestions = cip.getLinkSuggestions();
			double sumOfIndividualScoresForCurrentCip = suggestions
				.parallelStream()
				.mapToDouble(LinkSuggestion::getTotalScore)
				.sum();

			if (sumOfIndividualScoresForCurrentCip == 0) {
				sumOfIndividualScoresForCurrentCip = 1.;
				nullCompensation = 1. / suggestions.size();
			}

			final double finalSumOfIndividualScoresForCurrentCip = sumOfIndividualScoresForCurrentCip;
			// Divide each score by the max value to scale it to [0,1]
			double finalNullCompensation = nullCompensation;
			suggestions
				.parallelStream()
				.forEach(score -> {
					LinkSuggestion linkSuggestion = this.linkSuggestions.get(score.getTargetElement().getKey());
					linkSuggestion.addToScore((score.getTotalScore() + finalNullCompensation) / (finalSumOfIndividualScoresForCurrentCip * this.cips.size()), cip.getName());//sumOfIndividualScoresForCurrentCip);
				});

		});
	}


	@Override
	public String getId() {
		return "BaseCalculation";
	}

	@Override
	public String getName() {
		return "BaseCalculation";
	}

}
