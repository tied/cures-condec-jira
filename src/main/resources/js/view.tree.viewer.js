/**
 called by
 * view.decision.knowledge.page.js
*/
function buildTreeViewer() {
	console.log("view.tree.viewer.js buildTreeViewer");
	resetTreeViewer();
	var rootElementType = $("select[name='select-root-element-type']").val();
	conDecAPI.getTreeViewer(rootElementType, function(core) {
		jQueryConDec("#jstree").jstree({
			"core" : core,
			"plugins" : [ "dnd", "contextmenu", "wholerow", "sort", "search", "state" ],
			"search" : {
				"show_only_matches" : true
			},
			"contextmenu" : {
				"items" : customMenu
			}
		});
		$("#jstree-search-input").keyup(function() {
			var searchString = $(this).val();
			jQueryConDec("#jstree").jstree(true).search(searchString);
		});
	});
	addDragAndDropSupportForTreeViewer();
}

/**
 local usage only
*/
function customMenu(node) {
	console.log("view.tree.viewer.js customMenu(node)");
	if (node.li_attr['class'] == "sentence") {
		return contextMenuActionsForSentences;
	} else {
		return contextMenuActions;
	}
}

/**
 called by
 * view.context.menu.js
 * locally
*/
function resetTreeViewer() {
	console.log("view.tree.viewer.js resetTreeViewer");
	var treeViewer = jQueryConDec("#jstree").jstree(true);
	if (treeViewer) {
		treeViewer.destroy();
	}
}
/**
 local usage only
*/
function getTreeViewerNodeById(nodeId) {
	console.log("view.tree.viewer.js getTreeViewerNodeById(nodeId)");
	if (nodeId === "#") {
		return nodeId;
	}
	return jQueryConDec("#jstree").jstree(true).get_node(nodeId);
}
/**
 called by 
 * view.decision.knowledge.js
*/
function selectNodeInTreeViewer(nodeId) {
	console.log("view.tree.viewer.js selectNodeInTreeViewer");
	jQueryConDec(document).ready(function() {
		var treeViewer = jQueryConDec("#jstree").jstree(true);
		if (treeViewer) {
		    treeViewer.deselect_all(true);
			treeViewer.select_node(nodeId);
		}
	});
}
/**
 called by 
 * view.tab.panel.js
 * locally
*/ 
function addDragAndDropSupportForTreeViewer() {
	console.log("view.tree.viewer.js addDragAndDropSupportForTreeViewer");
	jQueryConDec("#jstree").on('move_node.jstree', function(object, nodeInContext) {
		var node = nodeInContext.node;
		var parentNode = getTreeViewerNodeById(nodeInContext.parent);
		var oldParentNode = getTreeViewerNodeById(nodeInContext.old_parent);
		var nodeId = node.data.id;

		console.log(!node.li_attr['class'] === "sentence");
		if (node.li_attr['class'] === "issue" && parentNode.li_attr['class'] === "issue" && oldParentNode.li_attr['class'] === "issue") {

			if (oldParentNode === "#" && parentNode !== "#") {
				
				conDecAPI.createLinkToExistingElement(parentNode.data.id, nodeId);
			}
			if (parentNode === "#" && oldParentNode !== "#") {
				
				conDecAPI.deleteLink(oldParentNode.data.id, nodeId, function() {
					notify();
				});
			}
			if (parentNode !== '#' && oldParentNode !== '#') {
				
				conDecAPI.deleteLink(oldParentNode.data.id, nodeId, function() {
					conDecAPI.createLinkToExistingElement(parentNode.data.id, nodeId);
				});
			}
		} else{

			var targetType = (parentNode.li_attr['class'] === "sentence") ? "s" : "i";

			if (oldParentNode === "#" && parentNode !== "#") {

				conDecAPI.linkGenericElements(parentNode.data.id, nodeId, targetType, "s", function() {
					refreshTreeViewer();
				});
			}
			if (parentNode === "#" && oldParentNode !== "#") {

				targetTypeOld = (oldParentNode.li_attr['class'] === "sentence") ? "s" : "i";
				conDecAPI.deleteGenericLink(oldParentNode.data.id, nodeId, targetTypeOld, "s", function() {
					refreshTreeViewer()
				});
			}
			if (parentNode !== '#' && oldParentNode !== '#') {

				targetTypeOld = (oldParentNode.li_attr['class'] === "sentence") ? "s" : "i";
				var nodeType = (node.li_attr['class'] === "sentence") ? "s" : "i";
				if (nodeType == "i" && targetTypeOld == "i") {
					conDecAPI.deleteLink(oldParentNode.data.id, nodeId, function() {
						conDecAPI.linkGenericElements(parentNode.data.id, nodeId, targetType, nodeType, function() {
							refreshTreeViewer()
						});
					});
				} else {
					conDecAPI.deleteGenericLink(oldParentNode.data.id, nodeId, targetTypeOld, nodeType, function() {
						conDecAPI.linkGenericElements(parentNode.data.id, nodeId, targetType, nodeType, function() {
							refreshTreeViewer()
						});
					});
				}
			}
		}

	});
}