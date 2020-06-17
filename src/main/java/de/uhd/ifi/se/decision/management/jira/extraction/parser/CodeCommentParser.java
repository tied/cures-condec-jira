package de.uhd.ifi.se.decision.management.jira.extraction.parser;

import java.io.File;
import java.util.List;

import de.uhd.ifi.se.decision.management.jira.model.git.CodeComment;

/**
 * Interface to extract the decision knowledge elements from code comments.
 */
public interface CodeCommentParser {

	/**
	 * Gets all {@link CodeComment}s found in the source file.
	 *
	 * @return comments with their positions in the source file.
	 */
	List<CodeComment> getComments(File inspectedFile);
}