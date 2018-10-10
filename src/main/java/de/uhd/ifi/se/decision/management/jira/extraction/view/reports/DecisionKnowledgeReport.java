package de.uhd.ifi.se.decision.management.jira.extraction.view.reports;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;

import de.uhd.ifi.se.decision.management.jira.extraction.model.GenericLink;
import de.uhd.ifi.se.decision.management.jira.extraction.model.Sentence;
import de.uhd.ifi.se.decision.management.jira.extraction.persistence.ActiveObjectsManager;
import de.uhd.ifi.se.decision.management.jira.extraction.view.reports.plotlib.Plotter;
import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.model.KnowledgeType;
import de.uhd.ifi.se.decision.management.jira.view.treant.Treant;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.ofbiz.core.entity.GenericEntityException;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class DecisionKnowledgeReport extends AbstractReport {
	@JiraImport
	private final SearchProvider searchProvider;
	@JiraImport
	private final ProjectManager projectManager;

	private Long projectId;

	private KnowledgeType rootType;

	public DecisionKnowledgeReport(SearchProvider searchProvider, ProjectManager projectManager,
			@JiraImport DateTimeFormatterFactory dateTimeFormatterFactory) {
		this.searchProvider = searchProvider;
		this.projectManager = projectManager;
	} 

	public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception {

		Map<String, Object> velocityParams = new HashMap<>();
		velocityParams.put("projectName", action.getProjectManager().getProjectObj(projectId).getName());
		
		//get Number of Comments per Issue
		List<Integer> numCommentsPerIssue = getNumberOfCommentsPerIssue(action.getLoggedInUser(), projectId);
		byte[] imgCommentsPerIssue = createBoxPlot(numCommentsPerIssue, "Number of Comments per JIRA Issue",
				"#Comments");
		velocityParams.put("imgCommentsPerIssue", new String(imgCommentsPerIssue));
		velocityParams.put("numCommentsPerIssue",
				buildVelocityString("Number of Comments per JIRA Issue", numCommentsPerIssue));

		//get Number of commits per Issue TODO:Access commit DB
		List<Integer> numCommitsPerIssue = getNumberOfCommitsPerIssue(action.getLoggedInUser(), projectId);
		byte[] imgCommitsPerIssue = createBoxPlot(numCommitsPerIssue, "Number of Commits per JIRA Issue", "#Commits");
		velocityParams.put("imgCommitsPerIssue", new String(imgCommitsPerIssue));
		velocityParams.put("numCommitsPerIssue",
				buildVelocityString("Number of Commits per JIRA Issue", numCommitsPerIssue));

		//Get associated Knowledge Types in Sentences per Issue
		Map<String, Integer> numKnowledgeTypesPerIssue = getDecKnowElementsPerIssue(action.getLoggedInUser(),
				projectId);
		byte[] imgKnowledgeTypesPerIssue = createPieChartImage(numKnowledgeTypesPerIssue,
				"Number of KnowledgeTypes per JIRA Issue");
		velocityParams.put("imgKnowledgeTypesPerIssue", new String(imgKnowledgeTypesPerIssue));
		velocityParams.put("numKnowledgeTypesPerIssue",
				buildVelocityString("Number of KnowledgeTypes per JIRA Issue", numKnowledgeTypesPerIssue));
		
		//Get types of decisions and alternatives linkes to Issue (e.g. has decision but no alternative)
		Map<String, Integer> numLinksToIssue = getAlternativeDecisionPerIssue(action.getLoggedInUser(), projectId);
		byte[] imgLinksToIssue = createPieChartImage(numLinksToIssue, "Linked Elements to Issue");
		velocityParams.put("imgLinksToIssue", new String(imgLinksToIssue));
		velocityParams.put("numLinksToIssue", buildVelocityString("Linked Elements to Issue", numLinksToIssue));

		//Get Number of Alternatives With Arguments
		Map<String, Integer> numAlternativeWoArgument = getAlternativeArguments(action.getLoggedInUser(), projectId);
		byte[] imgAlternativeWoArgument = createPieChartImage(numAlternativeWoArgument, "Alternatives with Arguments");
		velocityParams.put("imgAlternativeWoArgument", new String(imgAlternativeWoArgument));
		velocityParams.put("numAlternativeWoArgument",
				buildVelocityString("Alternatives with Arguments", numAlternativeWoArgument));

		//Get Link Distance
		List<Integer> numLinkDistance = getLinkDistance(action.getLoggedInUser(), projectId);
		byte[] imgLinkDistance = createBoxPlot(numLinkDistance, this.rootType + " Link Distance",
				"Link distance from " + this.rootType);
		velocityParams.put("imgLinkDistance", new String(imgLinkDistance));
		velocityParams.put("numLinkDistance", buildVelocityString(this.rootType + " Link Distance", numLinkDistance));

		return descriptor.getHtml("view", velocityParams);
	}

	private List<Integer> getLinkDistance(ApplicationUser loggedInUser, Long projectId2) throws GenericEntityException {
		List<Integer> linkDistances = new ArrayList<>();

		List<DecisionKnowledgeElement> listOfIssues = ActiveObjectsManager
				.getAllElementsFromAoByType(projectManager.getProjectObj(projectId).getKey(), this.rootType);

		for (DecisionKnowledgeElement currentAlternative : listOfIssues) {
			Treant treant = new Treant(currentAlternative.getProject().getProjectKey(), currentAlternative.getKey(),
					100);
			linkDistances.add(treant.getRealDepth());
		}

		return linkDistances;
	}

	private Map<String, Integer> getAlternativeArguments(ApplicationUser loggedInUser, Long projectId2) {
		int alternativesHaveArgument = 0;
		int alternativesHaveNoArgument = 0;

		List<DecisionKnowledgeElement> listOfIssues = ActiveObjectsManager.getAllElementsFromAoByType(
				projectManager.getProjectObj(projectId).getKey(), KnowledgeType.ALTERNATIVE);

		for (DecisionKnowledgeElement currentAlternative : listOfIssues) {
			List<GenericLink> links = ActiveObjectsManager.getGenericLinksForElement("s" + currentAlternative.getId(),
					false);
			boolean hasArgument = false;
			for (GenericLink link : links) {
				DecisionKnowledgeElement dke = link.getOpposite("s" + currentAlternative.getId());
				if (dke instanceof Sentence) {
					if (((Sentence) dke).getArgument().equalsIgnoreCase("Pro")) {
						hasArgument = true;
					}
				}
			}
			if (hasArgument) {
				alternativesHaveArgument++;
			} else {
				alternativesHaveNoArgument++;
			}
		}
		Map<String, Integer> dkeCount = new HashMap<String, Integer>();
		dkeCount.put("Alternative with Argument", alternativesHaveArgument);
		dkeCount.put("Alternative without Argument", alternativesHaveNoArgument);

		return dkeCount;
	}

	private Map<String, Integer> getAlternativeDecisionPerIssue(ApplicationUser loggedInUser, Long projectId2)
			throws SearchException {
		Integer[] statistics = new Integer[4];
		Arrays.fill(statistics,0);
		List<DecisionKnowledgeElement> listOfIssues = ActiveObjectsManager
				.getAllElementsFromAoByType(projectManager.getProjectObj(projectId).getKey(), KnowledgeType.ISSUE);

		for (DecisionKnowledgeElement issue : listOfIssues) {
			List<GenericLink> links = ActiveObjectsManager.getGenericLinksForElement("s" + issue.getId(), false);
			boolean hasAlternative = false;
			boolean hasDecision = false;

			for (GenericLink link : links) {
				DecisionKnowledgeElement dke = link.getOpposite("s" + issue.getId());
				if (dke instanceof Sentence && dke.getType().equals(KnowledgeType.ALTERNATIVE)) {
					hasAlternative = true;
				} else if (dke instanceof Sentence && dke.getType().equals(KnowledgeType.DECISION)) {
					hasDecision = true;
				}
			}
			if (hasAlternative && hasDecision) {
				statistics[0]=statistics[0]+1;
			} else if (hasAlternative && !hasDecision) {
				statistics[1]=statistics[1]+1;
			} else if (!hasAlternative && hasDecision) {
				statistics[2]=statistics[2]+1;
			} else if (!hasAlternative && !hasDecision) {
				statistics[3]=statistics[3]+1;
			}
		}
		// Hashmaps as counter suck
		Map<String, Integer> dkeCount = new HashMap<String, Integer>();
		dkeCount.put("Has Alternative & Decision", statistics[0]);
		dkeCount.put("Has Alternative but no Decision", statistics[1]);
		dkeCount.put("Has Decision but no Alternative", statistics[2]);
		dkeCount.put("Has no Decision & Alternative", statistics[3]);

		return dkeCount;
	}

	private Map<String, Integer> getDecKnowElementsPerIssue(ApplicationUser loggedInUser, Long projectId2)
			throws SearchException {
		Map<String, Integer> dkeCount = new HashMap<String, Integer>();

		for (KnowledgeType type : KnowledgeType.getDefaulTypes()) {
			dkeCount.put(type.toString(), ActiveObjectsManager
					.getAllElementsFromAoByType(projectManager.getProjectObj(projectId).getKey(), type).size());
		}
		return dkeCount;
	}

	private List<Integer> getNumberOfCommitsPerIssue(ApplicationUser loggedInUser, Long projectId2)
			throws SearchException {
		JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
		SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);

		com.atlassian.query.Query query = jqlClauseBuilder.project(projectId2).buildQuery();
		com.atlassian.jira.issue.search.SearchResults searchResults = null;

		searchResults = searchService.search(loggedInUser, query, PagerFilter.getUnlimitedFilter());

		List<Integer> commentList = new ArrayList<>();
		for (Issue issue : searchResults.getIssues()) {
			commentList.add(ComponentAccessor.getCommentManager().getComments(issue).size());
		}
		return commentList;
	}

	private List<Integer> getNumberOfCommentsPerIssue(ApplicationUser user, Long projectId) throws SearchException {
//		user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
		JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
		SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
 
		com.atlassian.query.Query query = jqlClauseBuilder.project(projectId).buildQuery();
		com.atlassian.jira.issue.search.SearchResults searchResults = null;

		searchResults = searchService.search(user, query, PagerFilter.getUnlimitedFilter());

		List<Integer> commentList = new ArrayList<>();
		for (Issue issue : searchResults.getIssues()) {
			commentList.add(ComponentAccessor.getCommentManager().getComments(issue).size());
		}
		return commentList;
	}

	private byte[] createEncodedByteArray(RenderedImage bimage) {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		try {
			ImageIO.write(bimage, "JPG", bOut);
		} catch (IOException e) {
			return null;
		}
		// Encode data using BASE64
		byte[] bytesEncoded = Base64.encodeBase64(bOut.toByteArray());

		return bytesEncoded;
	}

	private String buildVelocityString(String name, List<Integer> list) {
		String result = name + ";";
		for (Integer i : list) {
			result += i + ";";
		}
		return result;
	}

	private String buildVelocityString(String name, Map<String, Integer> knowledgeTypesPerIssue) {
		String result = name + ";";
		for (String key : knowledgeTypesPerIssue.keySet()) {
			result += key.toString() + ": " + knowledgeTypesPerIssue.get(key).toString() + ";";
		}
		return result;
	}

	private byte[] createPieChartImage(Map<String, Integer> map, String title) {
		BufferedImage image = Plotter.getPieChart(title, map, true);
		return createEncodedByteArray(image);
	}

	private byte[] createBoxPlot(List<Integer> list, String title, String yaxis) {
		BufferedImage image = Plotter.getBoxPlot(title, yaxis, list);
		return createEncodedByteArray(image);
	}

	public void validate(ProjectActionSupport action, Map params) {
		projectId = ParameterUtils.getLongParam(params, "selectedProjectId");
		this.rootType = KnowledgeType.getKnowledgeType(ParameterUtils.getStringParam(params, "rootType"));
		;
	}
}