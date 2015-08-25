
/**
 *   Activating drag and drop for a given div
 *
 *   @param {string} divName: name of the div element to activate drag and drop for
 */
function activateDragAndDropEAE(divName) {
    var div = Ext.get(divName);
    var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
    dtgI.notifyDrop = dropTree;
}

function dropTree(source, e, data) {
    var targetdiv=this.el;
    //This tells us whether it is a numeric or character node.
    var val = data.node.attributes.oktousevalues;

    //Reset the alpha/numeric flag so we don't get the popup for entering a value.
    data.node.attributes.oktousevalues = "N";

    //Add the item to the input.
    var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));

    //Set back to original value
    data.node.attributes.oktousevalues = val;

    return true
}
/**
 *   Clears drag & drop selections from the given div
 *
 *   @param {string} divName: name of the div element to clear
 */
function clearVarSelection(divName) {
    var div = Ext.get(divName).dom;
    while (div.firstChild) {
        div.removeChild(div.firstChild);
    }
}

/**
 *   Returns the concepts defined via drag & drop from the given div
 *
 *   @param {string} divName: name of the div to get the selected concepts from
 *   @return {string[]}: array of found concepts
 */
function getConcepts(divName) {
    var div = Ext.get(divName);
    div = div.dom;
    var variables = [];
    for (var i = 0, len = div.childNodes.length; i < len; i++) {
        variables.push(div.childNodes[i].getAttribute('conceptid'));
    }
    return variables;
}

var conceptBoxes = [];
var sanityCheckErrors = [];
function registerConceptBoxEAE(name, cohort, type, min, max) {
    var concepts = getConcepts(name);
    var check1 = containsOnly(name, type);
    var check2 = min === undefined || concepts.length >= min;
    var check3 = max === undefined || concepts.length <= max;
    var check4 = concepts.length === 0 || !isSubsetEmpty(cohort);
    sanityCheckErrors.push(
        !check1 ? 'Concept box (' + name + ') contains concepts with invalid type! Valid type: ' + type :
            !check2 ? 'Concept box (' + name + ') contains too few concepts! Valid range: ' + min + ' - ' + max :
                !check3 ? 'Concept box (' + name + ') contains too many concepts! Valid range: ' + min + ' - ' + max :
                    !check4 ? 'Concept box (' + name + ') contains concepts but you have not specified any cohort for it!' : '');
    conceptBoxes.push({name: name, cohort: cohort, type: type, concepts: concepts});
}

/**
 *   Renders the input form for entering the parameters for a visualization/script
 */
function goToSmartRScript() {
    jQuery.ajax({
        url: pageInfo.basePath + '/eae/goToSmartR' ,
        type: "POST",
        timeout: '600000'
    }).done(function(serverAnswer) {
        jQuery("#index").html(serverAnswer);
    }).fail(function() {
        jQuery("#index").html("AJAX CALL FAILED!");
    });
}

/**
 *   Renders the input form for entering the parameters for a visualization/script
 */
function changeEAEInput(){
    jQuery("#outputs").html("");

    jQuery.ajax({
        url: pageInfo.basePath + '/eae/renderInputs',
        type: "POST",
        timeout: '600000',
        data: {'script': jQuery('#hpcscriptSelect').val()}
    }).done(function(serverAnswer) {
        jQuery("#inputs").html(serverAnswer);
    }).fail(function() {
        jQuery("#inputs").html("AJAX CALL FAILED!");
    });

}

/**
 *   Run a script from the eae
 */
function runEAEScript(){


}

/**
 *   Run a pathway enrichment from the eae
 */
function runPE(list){
    jQuery.ajax({
        url: pageInfo.basePath + '/eae/runPEForSelectedGenes',
        type: "POST",
        timeout: '600000',
        data: {'genesList': list}
    }).done(function(serverAnswer) {
        jQuery("#outputs").html(serverAnswer);
    }).fail(function() {
        jQuery("#outputs").html("AJAX CALL FAILED!");
    });
}

/**
 *   get the inpu from datasetexplorer
 */
function getClinicalMetaDataforEAE(){

    jQuery.ajax({
        url: pageInfo.basePath + '/eae/getClinicalMetaDataforEAE',
        type: "POST",
        timeout: '600000'
    }).done(function(serverAnswer) {
        jQuery("#selectedCohort").html(serverAnswer);
    }).fail(function() {
        jQuery("#selectedCohort").html("AJAX CALL FAILED!");
    });
}

/**
 *  Get the input List from the user
 */

function genesListData(){
    var data = [];
    data.push({list: 'ListOfGenes', value: jQuery('#genes').val()});
    return data
}