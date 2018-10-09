package de.uhd.ifi.se.decision.management.jira.view;

import com.atlassian.query.clause.Clause;
import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElementImpl;
import com.atlassian.jira.bc.issue.search.*;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GraphFiltering {
	private String query;
	private String projectKey;
	private boolean queryIsJQL;
	private boolean queryIsFilter;
	private boolean queryContainsCreationDate;
	private ApplicationUser user;
	private List<DecisionKnowledgeElement> queryResults;
	private long startDate;
	private long endDate;

	public GraphFiltering(String projectKey, String query, ApplicationUser user) {
		this.query = query;
		this.projectKey = projectKey;
		this.user = user;
		this.queryResults = new ArrayList<>();
		this.queryIsFilter = false;
		this.queryIsJQL = false;
		this.queryContainsCreationDate = false;
		this.startDate = -1;
		this.endDate = -1;
	}

	public void checkQueryType() {
		if (this.query.indexOf("jql") == 1) {
			this.queryIsJQL = true;
		} else if (this.query.indexOf("filter") == 1) {
			this.queryIsFilter = true;
		}
	}

	private String cropQuery() {
		String croppedQuery = "";
		System.out.println(this.query);
		String[] split = this.query.split("§");
		if (split.length > 1) {
			this.query = "?" + split[1];
		}
		if (this.query.indexOf("jql") == 1) {
			this.queryIsJQL = true;
		} else if (this.query.indexOf("filter") == 1) {
			this.queryIsFilter = true;
		}
		if (this.queryIsJQL) {
			croppedQuery = this.query.substring(5, this.query.length());
		} else if (this.queryIsFilter) {
			croppedQuery = this.query.substring(8, this.query.length());
		}
		return croppedQuery;
	}

	public void produceResultsFromQuery() {
		String filteredQuery = cropQuery();
		String finalQuery = "";
		List<Issue> resultingIssues = new ArrayList<>();
		if (queryIsFilter) {

			long filterId = 0;
			boolean filterIsNumberCoded = false;
			try {
				filterId = Long.parseLong(filteredQuery, 10);
				filterIsNumberCoded = true;
			} catch (NumberFormatException n) {
				//n.printStackTrace();
			}
			if (filterIsNumberCoded) {
				if (filterId <= 0) {
					switch ((int) filterId) {
						case 0: // Any other than the preset Filters
							finalQuery = "type != null";
							break;
						case -1: // My open issues
							finalQuery = "assignee = currentUser() AND resolution = Unresolved";
							break;
						case -2: // Reported by me
							finalQuery = "reporter = currentUser()";
							break;
						case -3: // Viewed recently
							finalQuery = "issuekey IN issueHistory()";
							break;
						case -4: // All issues
							finalQuery = "type != null";
							break;
						case -5: // Open issues
							finalQuery = "resolution = Unresolved";
							break;
						case -6: // Created recently
							finalQuery = "created >= -1w";
							break;
						case -7: // Resolved recently
							finalQuery = "resolutiondate >= -1w";
							break;
						case -8: // Updated recently
							finalQuery = "updated >= -1w";
							break;
						case -9: // Done issues
							finalQuery = "statusCategory = Done";
							break;
					}
					finalQuery = "Project = " + projectKey + " AND " + finalQuery;
				} else {
					SearchRequestManager srm = ComponentAccessor.getComponentOfType(SearchRequestManager.class);
					SearchRequest filter = srm.getSharedEntity(filterId);
					finalQuery = filter.getQuery().getQueryString();
				}
			} else {
				switch (filteredQuery) {
					case "myopenissues": // My open issues
						finalQuery = "assignee = currentUser() AND resolution = Unresolved";
						break;
					case "reportedbyme": // Reported by me
						finalQuery = "reporter = currentUser()";
						break;
					case "recentlyviewed": // Viewed recently
						finalQuery = "issuekey IN issueHistory()";
						break;
					case "allissues": // All issues
						finalQuery = "type != null";
						break;
					case "allopenissues": // Open issues
						finalQuery = "resolution = Unresolved";
						break;
					case "addedrecently": // Created recently
						finalQuery = "created >= -1w";
						break;
					case "updatedrecently": // Updated recently
						finalQuery = "updated >= -1w";
						break;
					case "resolvedrecently": // Resolved recently
						finalQuery = "resolutiondate >= -1w";
						break;
					case "doneissues": // Done issues
						finalQuery = "statusCategory = Done";
						break;
					default:
						this.query =  "type != null";
						break;
				}
			}
		} else if (queryIsJQL) {
			finalQuery = cropQuery();
		} else if (!queryIsJQL && !queryIsFilter) {
			finalQuery = "type = null";
		}

		System.out.println("Final Query: " + finalQuery);
		final SearchService.ParseResult parseResult =
				ComponentAccessor.getComponentOfType(SearchService.class).parseQuery(this.user, finalQuery);

		if (parseResult.isValid())

		{
			List<Clause> clauses = parseResult.getQuery().getWhereClause().getClauses();
			for (Clause clause : clauses) {
				if (clause.getName().equals("created")) {
					this.queryContainsCreationDate = true;
					long todaysDate = new Date().getTime();
					System.out.println("Todays Date: " + todaysDate);
				}
				System.out.println("Clause: " + clause);
			}
			try {
				final SearchResults results = ComponentAccessor.getComponentOfType(SearchService.class).search(
						this.user, parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
				resultingIssues = results.getIssues();

			} catch (SearchException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println(parseResult.getErrors().toString());
		}
		if (resultingIssues != null) {
			for (Issue issue : resultingIssues) {
				queryResults.add(new DecisionKnowledgeElementImpl(issue));
			}
		}

	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public ApplicationUser getUser() {
		return user;
	}

	public void setUser(ApplicationUser user) {
		this.user = user;
	}

	public boolean isQueryIsJQL() {
		return queryIsJQL;
	}

	public void setQueryIsJQL(boolean queryIsJQL) {
		this.queryIsJQL = queryIsJQL;
	}

	public boolean isQueryIsFilter() {
		return queryIsFilter;
	}

	public void setQueryIsFilter(boolean queryIsFilter) {
		this.queryIsFilter = queryIsFilter;
	}

	public List<DecisionKnowledgeElement> getQueryResults() {
		return queryResults;
	}

	public void setQueryResults(List<DecisionKnowledgeElement> queryResults) {
		this.queryResults = queryResults;
	}

	public boolean isQueryContainsCreationDate() {
		return queryContainsCreationDate;
	}
}