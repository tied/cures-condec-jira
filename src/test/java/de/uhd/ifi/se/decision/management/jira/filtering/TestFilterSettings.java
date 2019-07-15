package de.uhd.ifi.se.decision.management.jira.filtering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import de.uhd.ifi.se.decision.management.jira.TestSetUp;
import de.uhd.ifi.se.decision.management.jira.filtering.impl.FilterSettingsImpl;
import de.uhd.ifi.se.decision.management.jira.model.DocumentationLocation;

public class TestFilterSettings extends TestSetUp {
	private FilterSettings filterSettings;
	private long createDate;

	@Before
	public void setUp() {
		init();
		createDate = -1;
		filterSettings = new FilterSettingsImpl("TEST", "?jql=project%20%3D%20CONDEC", null);
	}

	@Test
	public void testDefaultConstructor() {
		assertNotNull(new FilterSettingsImpl());
	}

	@Test
	public void testKeySearchConstructor() {
		assertNotNull(new FilterSettingsImpl("TEST", "search string"));
	}

	@Test
	public void testGetProjectKey() {
		assertEquals(filterSettings.getProjectKey(), "TEST");
	}

	@Test
	public void testSetProjectKey() {
		filterSettings.setProjectKey("NEWTEST");
		assertEquals(filterSettings.getProjectKey(), "NEWTEST");
	}

	@Test
	public void testGetSearchString() {
		assertEquals("project = CONDEC", filterSettings.getSearchString());
	}

	@Test
	public void testSetSearchString() {
		filterSettings.setSearchString(filterSettings.getSearchString() + "TEST ENDING");
		assertEquals("project = CONDEC" + "TEST ENDING", filterSettings.getSearchString());
		filterSettings.setSearchString(null);
		assertEquals("", filterSettings.getSearchString());
	}

	@Test
	public void testGetCreatedEarliest() {
		assertEquals(createDate, filterSettings.getCreatedEarliest());
	}

	@Test
	public void testSetCreatedEarliest() {
		filterSettings.setCreatedEarliest(createDate - 50);
		assertEquals(createDate - 50, filterSettings.getCreatedEarliest());
	}

	@Test
	public void testGetCreatedLatest() {
		assertEquals(createDate, filterSettings.getCreatedLatest());
	}

	@Test
	public void testSetCreatedLatest() {
		filterSettings.setCreatedLatest(createDate + 10);
		assertEquals(createDate + 10, filterSettings.getCreatedLatest());
	}

	@Test
	public void testGetDocumentationLocations() {
		assertEquals(5, filterSettings.getDocumentationLocations().size());
		assertEquals(5, filterSettings.getNamesOfDocumentationLocations().size());
	}

	@Test
	public void testSetDocumentationLocations() {
		List<String> documentationLocations = DocumentationLocation.getAllDocumentationLocations().stream()
				.map(DocumentationLocation::toString).collect(Collectors.toList());
		filterSettings.setDocumentationLocations(documentationLocations);
		assertEquals(5, filterSettings.getDocumentationLocations().size());
		filterSettings.setDocumentationLocations(null);
		assertEquals(0, filterSettings.getDocumentationLocations().size());
	}

	@Test
	public void testGetNamesOfSelectedJiraIssueTypes() {
		assertEquals(5, filterSettings.getNamesOfSelectedJiraIssueTypes().size());
		filterSettings = new FilterSettingsImpl("TEST", "?jql=issuetype in (Decision, Issue)", null);
		assertEquals(2, filterSettings.getNamesOfSelectedJiraIssueTypes().size());
	}

	@Test
	public void testSetSelectedJiraIssueTypes() {
		filterSettings.setSelectedJiraIssueTypes(null);
		assertEquals(5, filterSettings.getNamesOfSelectedJiraIssueTypes().size());
	}
}