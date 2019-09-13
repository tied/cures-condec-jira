package de.uhd.ifi.se.decision.management.jira.releasenotes;

import de.uhd.ifi.se.decision.management.jira.releasenotes.impl.ReleaseNoteConfigurationImpl;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.EnumMap;
import java.util.List;

/**
 * Interface for release notes
 */
@JsonDeserialize(as = ReleaseNoteConfigurationImpl.class)
public interface ReleaseNoteConfiguration {

	/**
	 * Get the title of the release note.
	 *
	 * @return title of the release note.
	 */
	String getTitle();


	/**
	 * Set the title of the release note.
	 *
	 * @param title of the release note.
	 */
	void setTitle(String title);


	/**
	 * Get the startDate of the release note.
	 *
	 * @return startDate of the release note.
	 */
	String getStartDate();


	/**
	 * Set the startDate of the release note.
	 *
	 * @param startDate of the release note.
	 */
	void setStartDate(String startDate);


	/**
	 * Get the end Date of the release note.
	 *
	 * @return timeRange of the release note.
	 */
	String getEndDate();


	/**
	 * Set the end date of the release note.
	 *
	 * @param endDate of the release note.
	 */
	void setEndDate(String endDate);

	/**
	 * Get the sprint id of the release note.
	 *
	 * @return timeRange of the release note.
	 */
	String getSprintId();

	/**
	 * Set the sprint id  of the release note.
	 *
	 * @param sprintId of the release note.
	 */
	void setSprintId(String sprintId);


	/**
	 * Get the targetGroup of the release note.
	 *
	 * @return targetGroup of the release note.
	 */
	TargetGroup getTargetGroup();

	/**
	 * Set the targetGroup of the release note.
	 *
	 * @param targetGroup of the release note.
	 */

	void setTargetGroup(TargetGroup targetGroup);

	/**
	 * Get the issueMetricWeight of the release note.
	 *
	 * @return JiraIssueMetric of the release note.
	 */
	EnumMap<JiraIssueMetric, Double> getJiraIssueMetricWeight();

	/**
	 * Set the issueMetricWeight of the release note.
	 *
	 * @param issueMetricWeight of the release note.
	 */
	void setJiraIssueMetricWeight(EnumMap<JiraIssueMetric, Double> issueMetricWeight);

	/**
	 * Get the list with mapped bug fix issues.
	 *
	 * @return List<Integer>
	 */
	List<Integer> getBugFixMapping();

	/**
	 * Set the list with mapped bug fix issues.
	 *
	 * @param bugFixMapping of the release note.
	 */
	void setBugFixMapping(List<Integer> bugFixMapping);

	/**
	 * Get the list with mapped bug fix issues.
	 *
	 * @return List<Integer>
	 */
	List<Integer> getFeatureMapping();

	/**
	 * Set the list with mapped bug fix issues.
	 *
	 * @param featureMapping of the release note.
	 */
	void setFeatureMapping(List<Integer> featureMapping);

	/**
	 * Get the list with mapped bug fix issues.
	 *
	 * @return List<Integer>
	 */
	List<Integer> getImprovementMapping();

	/**
	 * Set the list with mapped bug fix issues.
	 *
	 * @param improvementMapping of the release note.
	 */
	void setImprovementMapping(List<Integer> improvementMapping);

	/**
	 * Get the hash mpa with the additional configuration
	 * @return EnumMap<AdditionalConfigurationOptions,Boolean>
	 */
	EnumMap<AdditionalConfigurationOptions,Boolean> getAdditionalConfiguration();

	/**
	 * Set the hash map with the additional configuration.
	 *
	 * @param additionalConfiguration of the release note.
	 */
 	void setAdditionalConfiguration(EnumMap<AdditionalConfigurationOptions,Boolean> additionalConfiguration);
}