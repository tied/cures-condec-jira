package de.uhd.ifi.se.decision.management.jira.extraction.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;

import de.uhd.ifi.se.decision.management.jira.extraction.CodeCommentParser;
import de.uhd.ifi.se.decision.management.jira.model.git.CodeComment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JavaCodeCommentParser  implements CodeCommentParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(CodeCommentParser.class);
	@Override
	public List<CodeComment> getComments(File inspectedFile) {

		CompilationUnit compilationUnit = parseJavaFile(inspectedFile);
		if (compilationUnit == null) {
			return null;
		} else {
			return getComments(compilationUnit);
		}
	}

	private CompilationUnit parseJavaFile(File inspectedFile) {
		CompilationUnit compilationUnit = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(inspectedFile.toString());
			compilationUnit = JavaParser.parse(fileInputStream);
			fileInputStream.close();
		} catch (ParseProblemException | IOException e) {
			LOGGER.error(e.getMessage());
		}
		return compilationUnit;
	}

	private List<CodeComment> getComments(CompilationUnit compilationUnit) {
		List<Comment> comments = compilationUnit.getComments();
		List<CodeComment> positionedComments =
			comments.stream()
				.filter(comment -> (comment.getBegin().isPresent()
						&& comment.getEnd().isPresent()))
				.map( comment -> {
					Position begin = comment.getBegin().get();
					Position end = comment.getEnd().get();
					return new CodeComment(comment.getContent()
							,begin.column, begin.line
							,end.column, end.line);
				})
				.collect(Collectors.toList());
		return positionedComments;
	}
}