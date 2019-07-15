package de.uhd.ifi.se.decision.management.jira.extraction.versioncontrol;

import de.uhd.ifi.se.decision.management.jira.extraction.RationaleFromCodeCommentExtractor;
import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.model.git.CodeComment;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * purpose: extract decision knowledge elements in the list of comments
 * of one source file associated with the diff change.
 * Extracted decision knowledge elements contain
 * the notion about their source within the source file.
 * Extracted rationale from comments is matched against diff entries.
 */
public class RationaleFromDiffCodeCommentExtractor {

	private EditList editList;
	private List<CodeComment> commentsInNewerFile;
	private List<CodeComment> commentsInOlderFile;
	private int cursorNewerFile = -1;
	private int cursorOlderFile = -1;

	public RationaleFromDiffCodeCommentExtractor(List<CodeComment> commentsInOlderFile
			, List<CodeComment> commentsInNewerFile
			, EditList editList) {
		this.commentsInNewerFile = commentsInNewerFile;
		this.commentsInOlderFile = commentsInOlderFile;
		this.editList = editList;
	}

	/**
	 * Moves comment cursor forward for the newer or older version file comments.
	 *
	 * @param: move newer instead of older file cursor?
	 * @return: success if cursor at nextInNewerFile comment exists.
	 */
	public boolean next(boolean forNewerFile) {
		if (forNewerFile) {
			cursorNewerFile++;
			return commentsInNewerFile != null
					&& (cursorNewerFile + 1) <= commentsInNewerFile.size();
		} else {
			cursorOlderFile++;
			return commentsInOlderFile != null
					&& (cursorOlderFile + 1) <= commentsInOlderFile.size();
		}
	}

	/**
	 * Extracts rationale from current comment.
	 * Makes a distinction if rationale was found within or outside of an edit.
	 *
	 * @param: comes the comment from the newer file version instead of older?
	 * @return: list of decision knowledge elements found in a comment.
	 */
	public Map<Edit, List<DecisionKnowledgeElement>> getRationaleFromComment(boolean newerFile
			, Map<Edit, List<DecisionKnowledgeElement>> elementsInSingleComment) {

		int cursor = cursorOlderFile;
		List<CodeComment> comments = commentsInOlderFile;
		if (newerFile) {
			cursor = cursorNewerFile;
			comments = commentsInNewerFile;
		}
		if ((cursor + 1) <= comments.size()) {
			CodeComment currentComment = comments.get(cursor);

			/*
			 @issue: A problem was observed within changes of branch
			  refs/remotes/origin/CONDEC-503.rest.API.feature.branch.rationale
			 for change on old JAVA file
			  src/main/java/de/uhd/ifi/se/decision/management/jira/extraction/impl/GitClientImpl.java
			  https://github.com/cures-hub/cures-condec-jira/pull/147/commits/847c56aaa0e71ee4c2bdf9d8e674f9dd92bf77b9
			  #diff-1e393b83bbc1e0b69baddee0f2897586L473
			 at lines 472 and 473.
			 The DECISION rationale was written on two single line comments, but only text
			 on the 1st line was taken over. 2nd line will not be classified as part of the rationale.

			 @alternative: Merge neighboured single line comments into commit blocks!
			 Only when they start at the same column and their line distance is 1.

			 @pro: less intrusive, tolerates developers' "mistakes" in comment usage
			 @con: propagates bad habits

			 @alternative: It is expected that multi line comments should be used for
			 storing rationale with multi line texts! No actions should be taken.

			 @pro: teaches developers a lesson to use comments correctly.
			 @con: not user friendly. Cannot assume every developer is using the comment options
			 of a language as intended.
			  */

			RationaleFromCodeCommentExtractor rationaleFromCodeComment =
					new RationaleFromCodeCommentExtractor(currentComment);
			List<DecisionKnowledgeElement> commentRationaleElements =
					rationaleFromCodeComment.getElements();

			// distinct rationale between changed and unchanged, only for newer version
			List<Edit> commentEdits = getEditsOnComment(currentComment, newerFile);
			// comment parts within diff
			if (commentEdits.size() > 0) {
				for (Edit edit : commentEdits) {
					List<DecisionKnowledgeElement> rationaleWithinEdit = getRationaleIntersectingEdit(edit
							, commentRationaleElements, newerFile);
					if (elementsInSingleComment.containsKey(edit)) {
						rationaleWithinEdit.addAll(elementsInSingleComment.get(edit));
					}
					elementsInSingleComment.put(edit, rationaleWithinEdit);

					// subtract edit-intersecting rationale from list of all rationale in a comment
					if (newerFile) {
						commentRationaleElements.removeAll(rationaleWithinEdit);
					}
				}
			}

			// return non-interseting elements
			if (newerFile && commentRationaleElements.size()>0) {

				if (elementsInSingleComment.containsKey(null)) {
					commentRationaleElements.addAll(elementsInSingleComment.get(null));
				}
				elementsInSingleComment.put(null, commentRationaleElements);
			}

		}
		return elementsInSingleComment;
	}

	private List<DecisionKnowledgeElement> getRationaleIntersectingEdit(Edit edit
			, List<DecisionKnowledgeElement> rationaleElements, boolean newerFile) {
		List<DecisionKnowledgeElement> filteredRationaleElements = new ArrayList<>();
		for (DecisionKnowledgeElement rationaleElement : rationaleElements) {
			if (doesRationaleIntersectWithEdit(rationaleElement, edit, newerFile)) {
				filteredRationaleElements.add(rationaleElement);
			}
		}
		return filteredRationaleElements;
	}

	private boolean doesRationaleIntersectWithEdit(DecisionKnowledgeElement rationaleElement, Edit edit, boolean newerFile) {
		int rationaleStart = RationaleFromCodeCommentExtractor.getRationaleStartLineInCode(rationaleElement);
		int rationaleEnd = RationaleFromCodeCommentExtractor.getRationaleEndLineInCode(rationaleElement);
		int editBegin = edit.getBeginA();
		int editEnd = edit.getEndA();

		if (newerFile) {
			editBegin = edit.getBeginB();
			editEnd = edit.getEndB();
		}

		// begin border inside rationale
		if (editBegin >= rationaleStart && editBegin <= rationaleEnd) {
			return true;
		}
		// end border inside rationale
		if (editEnd >= rationaleStart && editEnd <= rationaleEnd) {
			return true;
		}
		// edit overlaps rationale
		if (editBegin <= rationaleStart && editEnd >= rationaleEnd) {
			return true;
		}

		return false;
	}

	/* fetches list of edits which affected the comment */
	private List<Edit> getEditsOnComment(CodeComment comment, boolean newerFile) {
		return editList.stream().filter(edit -> {
					int begin = edit.getBeginA();
					int end = edit.getEndA();
					if (newerFile) {
						begin = edit.getBeginB();
						end = edit.getEndB();
					}
					return
							// change's end within the comment
							(end >= comment.beginLine
									&& end <= comment.endLine)
									||
									// change's begin within the comment
									(begin >= comment.beginLine
											&& begin <= comment.endLine)
									||
									// change overlaps comment
									(begin <= comment.beginLine
											&& end >= comment.endLine);

				}
		).collect(Collectors.toList());
	}

	/**
	 *  commentsInNewer/OlderFile contains a list of comments sorted by their
	 *  appearance order in source file, therefore already visited items in editList
	 *  could be removed from that list to improve algorithm runtime for files with
	 *  many small changes.
	 */
	/*private void removeNotNeededEdits(CodeComment currentComment) {

		editList.removeIf(edit ->
				edit.getEndB() < currentComment.beginLine);
	}*/
}