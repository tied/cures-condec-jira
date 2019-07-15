package de.uhd.ifi.se.decision.management.jira.view.vis;

import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElement;

import javax.xml.bind.annotation.XmlElement;

/**
 * Model class for vis.js Node.
 */
public class VisNode {
	@XmlElement
	private String id;

	@XmlElement
	private String label;

	@XmlElement
	private String title;

	@XmlElement
	private String group;

	@XmlElement
	private int level;

	@XmlElement
	private int cid;

	public VisNode() {
	}

	public VisNode(DecisionKnowledgeElement element, boolean collapsed, int level, int cid) {
		this.setId(element.getId() + "_" + element.getDocumentationLocationAsString());
		this.level = level;
		this.cid = cid;
		if (collapsed) {
			this.setGroup(element.getTypeAsString().toLowerCase());
			String summary;
			if (element.getSummary().length() > 100) {
				summary = element.getSummary().substring(0, 99) + "...";
			} else {
				summary = element.getSummary();
			}
			this.setLabel(element.getTypeAsString().toUpperCase() + "\n" + summary);
		} else {
			this.setGroup("collapsed");
			this.setLabel("");
		}
		this.setTitle("<b>" + element.getTypeAsString().toUpperCase() + " <br> " + element.getKey() + ":</b> "
				+ element.getSummary() + "<br> <i>" + element.getDescription() + "</i>");
	}

	public VisNode(DecisionKnowledgeElement element, String type, boolean collapsed, int level, int cid) {
		this(element, collapsed, level, cid);
		if (collapsed) {
			this.setGroup(type);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}
}