package de.uhd.ifi.se.decision.management.jira.view.treeviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import com.google.common.collect.ImmutableMap;

import de.uhd.ifi.se.decision.management.jira.ComponentGetter;
import de.uhd.ifi.se.decision.management.jira.extraction.model.Sentence;
import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.model.KnowledgeType;
import de.uhd.ifi.se.decision.management.jira.model.Link;

/**
 * Model class for Tree Viewer nodes
 */
public class Data {

	@XmlElement
	private String id;

	@XmlElement
	private String text;

	@XmlElement
	private List<Data> children;

	@XmlElement(name = "data")
	private DecisionKnowledgeElement nodeInfo;

	@XmlElement
	private String icon;

	@XmlElement(name = "a_attr")
	private Map<String, String> a_attr;

	@XmlElement(name = "li_attr")
	private Map<String, String> li_attr;

	public Data() {
	}

	public Data(DecisionKnowledgeElement decisionKnowledgeElement) {
		this.id = "tv"+ String.valueOf(decisionKnowledgeElement.getId());
		this.text = decisionKnowledgeElement.getSummary();
		this.icon = KnowledgeType.getIconUrl(decisionKnowledgeElement);
		this.nodeInfo = decisionKnowledgeElement;
		if (decisionKnowledgeElement.getDescription() != null && !decisionKnowledgeElement.getDescription().equals("")
				&& !decisionKnowledgeElement.getDescription().equals("undefined")) {
			this.a_attr = ImmutableMap.of("title", decisionKnowledgeElement.getDescription());
		}
		this.li_attr = ImmutableMap.of("class", "issue");
		if (decisionKnowledgeElement instanceof Sentence) {
			this.li_attr = ImmutableMap.of("class", "sentence", "sid", "s" + decisionKnowledgeElement.getId());
			checkTypeOfArgumentForSentenceEntity((Sentence) decisionKnowledgeElement);
		}
	}

	public Data(DecisionKnowledgeElement decisionKnowledgeElement, Link link) {
		this(decisionKnowledgeElement);
		this.icon = KnowledgeType.getIconUrl(decisionKnowledgeElement, link.getType());
		if (decisionKnowledgeElement instanceof Sentence) {
			checkTypeOfArgumentForSentenceEntity((Sentence) decisionKnowledgeElement);
		}
	}

	private void checkTypeOfArgumentForSentenceEntity(Sentence decisionKnowledgeElement) {
		if (((Sentence) decisionKnowledgeElement).getArgument().equalsIgnoreCase("pro")) {
			this.icon = ComponentGetter.getUrlOfImageFolder() + "argument_pro.png";
		}
		if (((Sentence) decisionKnowledgeElement).getArgument().equalsIgnoreCase("con")) {
			this.icon = ComponentGetter.getUrlOfImageFolder() + "argument_con.png";
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Data> getChildren() {
		if(children == null) {
			return new ArrayList<Data>();
		}
		return children;
	}

	public void setChildren(List<Data> children) {
		this.children = children;
	}

	public DecisionKnowledgeElement getNode() {
		return nodeInfo;
	}

	public void setNodeInfo(DecisionKnowledgeElement nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}