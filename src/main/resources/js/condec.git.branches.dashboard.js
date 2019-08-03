/*
 This module fills the box plots and pie charts used in the feature task branch dashboard item.

 Requires
 * js/condec.report.js

 Is referenced in HTML by
 * featureBranchesDashboardItem.vm
 */

var CONDEC_branchesQuality = [];

(function(global) {
    var dashboards = {};    /* TODO: object muss support more than one dashboard.*/

    var dashboardUID;
    var processing = null;
    var projectKey = null;

    var dashboardContentNode;
    var dashboardDataErrorNode;
    var dashboardFatalErrorNode;
    var dashboardNoContentsNode;
    var dashboardProcessingNode;

	var ConDecBranchesDashboard = function ConDecBranchesDashboard() {
		console.log("ConDecBranchesDashboard constructor");
	};

    ConDecBranchesDashboard.prototype.init = function init(_projectKey, _dashboardUID) {
        console.log("received for project: "+ _projectKey +" UID:"+ _dashboardUID);
        projectKey = _projectKey
        dashboardUID = _dashboardUID

        getHTMLNodes( "condec-branches-dashboard-contents-container"+dashboardUID
        , "condec-branches-dashboard-contents-data-error"+dashboardUID
        , "condec-branches-dashboard-no-project"+dashboardUID
        , "condec-branches-dashboard-processing"+dashboardUID);

        getBranches(projectKey)
    }

    function getHTMLNodes(containerName, dataErrorName, noProjectName, processingName) {
        if (!dashboardContentNode) {
            dashboardContentNode   = document.getElementById(containerName)
            dashboardDataErrorNode = document.getElementById(dataErrorName)
            dashboardNoContentsNode = document.getElementById(noProjectName)
            dashboardProcessingNode = document.getElementById(processingName)
        }
    }
    function showDashboardSection(node) {
        var hiddenClass = "hidden";
        dashboardContentNode.classList.add(hiddenClass);
        dashboardDataErrorNode.classList.add(hiddenClass);
        dashboardNoContentsNode.classList.add(hiddenClass);
        dashboardProcessingNode.classList.add(hiddenClass);
        node.classList.remove(hiddenClass)
    }

    function getBranches(projectKey){
        if (!projectKey || !projectKey.length || !projectKey.length>0) {
            return;
        }
        /* on XHR HTTP failure codes the code aborts instead of processing with processDataBad() !?
        if (processing) {
            return warnStillProcessing();
        }
        */
        processing = projectKey;
        showDashboardSection(dashboardProcessingNode);

        url = AJS.contextPath()
          + "/rest/decisions/latest/view/elementsFromBranchesOfProject.json?projectKey="
          + projectKey

        // get cache or server data?
        if (localStorage.getItem("condec.restCacheTTL")) {
          console.log("condec.restCacheTTL setting found");
          if (localStorage.getItem(url)) {
            var data = null;
            var now = Date.now();
            var cacheTTL = parseInt(localStorage.getItem("condec.restCacheTTL"));
            try {
              data = JSON.parse(localStorage.getItem(url));
            } catch (ex) {
              data = null;
            }
            if (data && cacheTTL) {
              if (now - data.timestamp < cacheTTL) {
                console.log(
                  "Cache is within specified TTL, therefore getting data from local cache instead from server."
                );
                return processData(data);
              } else {
                console.log("Cache TTL expired, therefore starting  REST query.");
              }
            }
            if (!cacheTTL) {
              console.log(
                "Cache TTL is not a number, therefore starting  REST query."
              );
            }
          }
        }
        console.log("Starting  REST query.")
        AJS.$.ajax({
          url: url,
          type: "get",
          dataType: "json",
          async: false,
          success: processData,
          error: processDataBad
        });
    }

    function processDataBad(data) {
        showDashboardSection(dashboardDataErrorNode)
        doneWithXhrRequest();
    }

    function processData(data) {
        doneWithXhrRequest();
        showDashboardSection(dashboardContentNode);
        data.timestamp = Date.now();
        localStorage.setItem(url, JSON.stringify(data, null, 1));
        processing = null;
        processBranches(data);
    }

    function doneWithXhrRequest() {
        dashboardProcessingNode.classList.remove("error");
        showDashboardSection(dashboardProcessingNode)
    }

    function warnStillProcessing() {
        dashboardProcessingNode.classList.add("error");
        console.warn("Still processing request for: "+processing);
    }

    function processBranches(data){
    var branches = data.branches;
    var branchesQuality = [];
    for (branchIdx = 0; branchIdx < branches.length; branchIdx++) {
          var lastBranch = conDecLinkBranchCandidates.extractPositions(branches[branchIdx]);

          // these elements are sorted by commit age and occurrence in message
          var lastBranchElementsFromMessages =
           lastBranch.elements.filter(function(e){return e.key.sourceTypeCommitMessage;});

          // these elements are not sorted, we want only B(final) files.
          var lastBranchElementsFromFiles_BUT_NotSorted =
           lastBranch.elements.filter(function(e){return e.key.codeFileB;})

          // sort file elements
          var lastBranchElementsFromFiles =
           conDecLinkBranchCandidates.sortRationaleDiffOfFiles(lastBranchElementsFromFiles_BUT_NotSorted);

          var lastBranchRelevantElementsSortedWithPosition =
           lastBranchElementsFromMessages.concat(lastBranchElementsFromFiles);

          // assess relations between rationale and their problems
          conDecLinkBranchCandidates.init(
            lastBranchRelevantElementsSortedWithPosition,
            lastBranch.branchName,
            branchIdx,
            ''
          );
          branchQuality = {};
          branchQuality.name = lastBranch.branchName;
          branchQuality.status = conDecLinkBranchCandidates.getBranchStatus();
          branchQuality.problems = conDecLinkBranchCandidates.getProblemNamesObserved();
          branchesQuality.push(branchQuality);
        }
        CONDEC_branchesQuality = branchesQuality;
    }

	global.conDecBranchesDashboard  = new ConDecBranchesDashboard ();
})(window);