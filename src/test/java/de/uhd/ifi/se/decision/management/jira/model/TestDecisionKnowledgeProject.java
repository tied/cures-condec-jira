package de.uhd.ifi.se.decision.management.jira.model;

import com.atlassian.jira.project.Project;
import de.uhd.ifi.se.decision.management.jira.TestSetUp;
import de.uhd.ifi.se.decision.management.jira.extraction.gitclient.TestSetUpGit;
import de.uhd.ifi.se.decision.management.jira.extraction.versioncontrol.GitRepositoryConfiguration;
import de.uhd.ifi.se.decision.management.jira.mocks.MockPluginSettings;
import de.uhd.ifi.se.decision.management.jira.mocks.MockPluginSettingsFactory;
import de.uhd.ifi.se.decision.management.jira.persistence.ConfigPersistenceManager;
import de.uhd.ifi.se.decision.management.jira.testdata.JiraProjects;
import de.uhd.ifi.se.decision.management.jira.testdata.JiraUsers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for a Jira project with the configuration settings used in this
 * plug-in.
 */
public class TestDecisionKnowledgeProject extends TestSetUp {
	private DecisionKnowledgeProject project;

	@Before
	public void setUp() {
		init();
		this.project = new DecisionKnowledgeProject(JiraProjects.getTestProject());
	}

	@Test
	public void testUnderlyingJiraProjectNull() {
		DecisionKnowledgeProject project = new DecisionKnowledgeProject((Project) null);
		assertEquals("", project.getProjectKey());
		assertEquals("", project.getProjectName());
	}

	@Test
	public void testConstructorWithProjectKey() {
		DecisionKnowledgeProject project = new DecisionKnowledgeProject("TEST");
		assertEquals(JiraProjects.getTestProject(), project.getJiraProject());
	}

	@Test
	public void testGetProjectKey() {
		assertEquals("TEST", project.getProjectKey());
	}

	@Test
	public void testGetProjectName() {
		assertEquals("TEST", project.getProjectName());
	}

	@Test
	public void testIsActivated() {
		assertEquals(true, project.isActivated());
	}

	@Test
	public void testIsJiraIssueStorage() {
		assertEquals(true, project.isIssueStrategy());
	}

	@Test
	public void testGetDecisionKnowledgeTypes() {
		assertEquals(18, project.getConDecKnowledgeTypes().size());
		assertEquals(18, project.getNamesOfConDecKnowledgeTypes().size());
	}

	@Test
	public void testIsKnowledgeExtractedFromGit() {
		assertEquals(true, project.isKnowledgeExtractedFromGit());
	}

	@Test
	public void testIsPostSquashedCommitsActivated() {
		assertEquals(true, project.isPostSquashedCommitsActivated());
	}

	@Test
	public void testIsPostFeatureBranchCommitsActivated() {
		assertEquals(true, project.isPostFeatureBranchCommitsActivated());
	}

	@Test
	public void testGetGitRepositoryConfigurations() {
		GitRepositoryConfiguration gitRepositoryConfiguration = new GitRepositoryConfiguration(TestSetUpGit.GIT_URI,
				"master", "HTTP", "heinz.guenther", "P@ssw0rd!");
		ConfigPersistenceManager.setGitRepositoryConfiguration("TEST", gitRepositoryConfiguration);
		List<GitRepositoryConfiguration> gitConfs = project.getGitRepositoryConfigurations();
		assertEquals("master", gitConfs.get(0).getDefaultBranch());
		assertEquals("HTTP", gitConfs.get(0).getAuthMethod());
		assertEquals("heinz.guenther", gitConfs.get(0).getUsername());
		assertEquals("P@ssw0rd!", gitConfs.get(0).getToken());
	}

	@Test
	public void testIsWebhookEnabled() {
		assertEquals(false, project.isWebhookEnabled());
	}

	@Test
	public void testGetWebhookUrl() {
		assertEquals("http://true", project.getWebhookUrl());
	}

	@Test
	public void testGetWebhookSecret() {
		ConfigPersistenceManager.setWebhookSecret(project.getProjectKey(), "myhoneybee");
		assertEquals("myhoneybee", project.getWebhookSecret());
	}

	@Test
	public void testIsClassifierEnabled() {
		assertEquals(false, project.getTextClassificationConfiguration().isActivated());
	}

	@Test
	public void testGetJiraIssueTypes() {
		assertEquals(6, project.getJiraIssueTypeNames().size());
	}

	@Test
	public void testGetKnowledgeTypesStorageInJiraIssuesActivated() {
		assertEquals(20, project.getNamesOfKnowledgeTypes().size());
	}

	@Test
	public void testGetLinkTypes() {
		assertEquals(4, DecisionKnowledgeProject.getJiraIssueLinkTypes().size());
		// currently, all Mock issue link types are called "relate"
		assertEquals(2, DecisionKnowledgeProject.getNamesOfLinkTypes().size());
	}

	@Test
	public void testGetProjectsWithConDecActivatedAndAccessableForUser() {
		assertEquals(1, DecisionKnowledgeProject
			.getProjectsWithConDecActivatedAndAccessableForUser(JiraUsers.SYS_ADMIN.getApplicationUser()).size());
	}

	@Test
	public void testGetAllNamesOfLinkTypes() {
		assertEquals(9, DecisionKnowledgeProject.getAllNamesOfLinkTypes().size());
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("Other"));
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("contains"));
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("attacks"));
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("supports"));
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("comments"));
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("is contained by"));
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("is attacked by"));
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("is supported by"));
		assertTrue(DecisionKnowledgeProject.getAllNamesOfLinkTypes().contains("is commented by"));
	}

	@AfterClass
	public static void tearDown() {
		// reset plugin settings to default settings
		MockPluginSettingsFactory.pluginSettings = new MockPluginSettings();
	}

}