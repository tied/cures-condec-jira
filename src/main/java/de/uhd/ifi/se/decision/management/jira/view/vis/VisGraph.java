package de.uhd.ifi.se.decision.management.jira.view.vis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import de.uhd.ifi.se.decision.management.jira.model.DecisionKnowledgeElement;
import de.uhd.ifi.se.decision.management.jira.model.Graph;
import de.uhd.ifi.se.decision.management.jira.model.Link;
import de.uhd.ifi.se.decision.management.jira.model.impl.GraphImpl;

@XmlRootElement(name = "vis")
@XmlAccessorType(XmlAccessType.FIELD)
public class VisGraph {
	@XmlElement
	private HashSet<VisNode> nodes;
	@XmlElement
	private HashSet<VisEdge> edges;
	@XmlElement
	private String rootElementKey;

	@JsonIgnore
	private Graph graph;
	@JsonIgnore
	private boolean isHyperlinked;
	@JsonIgnore
	private List<DecisionKnowledgeElement> elementsAlreadyAsNode;
	@JsonIgnore
	private List<DecisionKnowledgeElement> elementsMatchingFilterCriteria;
	@JsonIgnore
	private int level;
	@JsonIgnore
	private int cid;

	public VisGraph() {
	}

	public VisGraph(List<DecisionKnowledgeElement> elements,String projectKey) {
		if(projectKey == null ){
			return;
		}
		this.graph = new GraphImpl(projectKey);
		this.cid = 0;
		this.level = 50;
		this.nodes = new HashSet<>();
		this.edges = new HashSet<>();
		this.setHyperlinked(false);
		if(elements== null || elements.size() ==0){
			this.nodes = new HashSet<>();
			this.edges = new HashSet<>();
			this.rootElementKey= "";
			return;
		}
		for(DecisionKnowledgeElement element: elements){
			this.nodes.add(new VisNode(element, true, level, cid));
			level += 1;
			cid += 1;
		}
		for(Link edge: graph.getAllLinks(elements)){
			this.edges.add(new VisEdge(edge));
		}


	}

	public VisGraph(DecisionKnowledgeElement rootElement, List<DecisionKnowledgeElement> elements) {
		this.elementsMatchingFilterCriteria = elements;
		this.rootElementKey = (rootElement.getId() + "_" + rootElement.getDocumentationLocationAsString());
		nodes = new HashSet<>();
		edges = new HashSet<>();
		elementsAlreadyAsNode = new ArrayList<>();
		level = 50;
		cid = 0;
		fillNodesAndEdges(rootElement, null, level, cid);
	}

	// TODO Reduce complexity of this function
	// Way too big needs to be split in small functions
	private void fillNodesAndEdges(DecisionKnowledgeElement element, Link link, int level, int cid) {
		if (element == null || element.getProject() == null) {
			return;
		}
		if (graph == null) {
			graph = new GraphImpl(element);
		}

		if (link != null) {
			switch (link.getType()) {
			case "support":
				if (element.getId() == link.getSourceElement().getId()) {
					if (!(this.elementsAlreadyAsNode.contains(element))) {
						this.elementsAlreadyAsNode.add(element);
						this.nodes.add(new VisNode(element, "pro", isCollapsed(element), level + 2, cid));
					}
				} else {
					if (!(this.elementsAlreadyAsNode.contains(element))) {
						this.elementsAlreadyAsNode.add(element);
						this.nodes.add(new VisNode(element, isCollapsed(element), level, cid));
					}
				}
				break;
			case "attack":
				if (element.getId() == link.getSourceElement().getId()) {
					if (!(this.elementsAlreadyAsNode.contains(element))) {
						this.elementsAlreadyAsNode.add(element);
						this.nodes.add(new VisNode(element, "con", isCollapsed(element), level + 2, cid));
					}
				} else {
					if (!(this.elementsAlreadyAsNode.contains(element))) {
						this.elementsAlreadyAsNode.add(element);
						this.nodes.add(new VisNode(element, isCollapsed(element), level, cid));
					}
				}
				break;
			default:
				if (!(this.elementsAlreadyAsNode.contains(element))) {
					this.elementsAlreadyAsNode.add(element);
					this.nodes.add(new VisNode(element, isCollapsed(element), level, cid));
				}
				break;
			}
			this.edges.add(new VisEdge(link));

		} else {
			if (!(this.elementsAlreadyAsNode.contains(element))) {
				this.elementsAlreadyAsNode.add(element);
				this.nodes.add(new VisNode(element, isCollapsed(element), level, cid));
			}
		}
		Map<DecisionKnowledgeElement, Link> childrenAndLinks = graph.getAdjacentElementsAndLinks(element);
		for (Map.Entry<DecisionKnowledgeElement, Link> childAndLink : childrenAndLinks.entrySet()) {
			if (childAndLink.getKey() != null) {
				if (childAndLink.getValue().getSourceElement().equals(element)) {
					this.level = level + 1;
				} else {
					this.level = level - 1;
				}
				this.cid = cid + 1;
				fillNodesAndEdges(childAndLink.getKey(), childAndLink.getValue(), this.level, this.cid);
			}
		}
	}

	private boolean isCollapsed(DecisionKnowledgeElement element) {
		return elementsMatchingFilterCriteria.contains(element);
	}

	public void setNodes(HashSet<VisNode> nodes) {
		this.nodes = nodes;
	}

	public void setEdges(HashSet<VisEdge> edges) {
		this.edges = edges;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;

	}

	public void setHyperlinked(boolean hyperlinked) {
		isHyperlinked = hyperlinked;
	}

	public HashSet<VisNode> getNodes() {
		return nodes;
	}

	public HashSet<VisEdge> getEdges() {
		return edges;
	}

	public Graph getGraph() {
		return graph;
	}

	public boolean isHyperlinked() {
		return isHyperlinked;
	}

	public String getRootElementKey() {
		return rootElementKey;
	}

	public void setRootElementKey(String rootElementKey) {
		this.rootElementKey = rootElementKey;
	}
}