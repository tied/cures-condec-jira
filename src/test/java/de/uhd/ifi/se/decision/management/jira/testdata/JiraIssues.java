package de.uhd.ifi.se.decision.management.jira.testdata;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.Project;

import de.uhd.ifi.se.decision.management.jira.extraction.TestTextSplitter;
import de.uhd.ifi.se.decision.management.jira.model.text.PartOfJiraIssueText;
import de.uhd.ifi.se.decision.management.jira.persistence.JiraIssueTextPersistenceManager;

public class JiraIssues {

	private static List<MutableIssue> jiraIssues;

	public static List<MutableIssue> getTestJiraIssues() {
		if (jiraIssues == null || jiraIssues.isEmpty()) {
			jiraIssues = createJiraIssues(JiraProjects.TEST.createJiraProject(1));
		}
		return jiraIssues;
	}

	public static List<MutableIssue> createJiraIssues(Project project) {
		List<MutableIssue> jiraIssues = new ArrayList<MutableIssue>();

		if (project == null) {
			return jiraIssues;
		}

		List<IssueType> jiraIssueTypes = JiraIssueTypes.getTestTypes();

		// Work items
		MutableIssue issue = createJiraIssue(1, jiraIssueTypes.get(0), project, "WI: Implement feature");
		jiraIssues.add(issue);
		issue = createJiraIssue(14, jiraIssueTypes.get(0), project, "WI: Yet another work item");
		jiraIssues.add(issue);
		issue = createJiraIssue(30, jiraIssueTypes.get(0), project, "WI: Do an interesting task");
		jiraIssues.add(issue);

		// Issue
		issue = createJiraIssue(2, jiraIssueTypes.get(1), project, "How can we implement the feature?");
		jiraIssues.add(issue);

		// Alternative
		issue = createJiraIssue(3, jiraIssueTypes.get(2), project, "We could do it like this!");
		jiraIssues.add(issue);
		issue.setParentId((long) 2);

		// Decision
		issue = createJiraIssue(4, jiraIssueTypes.get(3), project, "We will do it like this!");
		jiraIssues.add(issue);
		issue.setParentId((long) 2);

		// Pro-Argument for the decision
		issue = createJiraIssue(5, jiraIssueTypes.get(4), project, "This is a great solution.");
		jiraIssues.add(issue);
		issue.setParentId((long) 5);
		return jiraIssues;
	}

	private static MutableIssue createJiraIssue(int id, IssueType issueType, Project project, String summary) {
		MutableIssue issue = new MockIssue(id, project.getKey() + "-" + id);
		((MockIssue) issue).setProjectId(project.getId());
		issue.setProjectObject(project);
		issue.setIssueType(issueType);
		issue.setSummary(summary);
		issue.setDescription(summary);
		issue.setCreated(new Timestamp(System.currentTimeMillis()));
		return issue;
	}

	public static Issue addComment(Issue issue) {
		List<PartOfJiraIssueText> comment = TestTextSplitter.getSentencesForCommentText("{issue} testobject {issue}");
		PartOfJiraIssueText sentence = comment.get(0);
		sentence.setJiraIssueId(issue.getId());
		JiraIssueTextPersistenceManager.insertDecisionKnowledgeElement(sentence,
				JiraUsers.SYS_ADMIN.getApplicationUser());

		return sentence.getJiraIssue();
	}

}