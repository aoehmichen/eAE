
/**
 *   Activating drag and drop for a given div
 *
 *   @param {string} divName: name of the div element to activate drag and drop for
 */
function activateDragAndDrop(divName) {
    var div = Ext.get(divName);
    var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
    dtgI.notifyDrop = dropOntoCategorySelection;
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
 *   get the inpu from datasetexplorer
 */
function getClinicalMetaDataforEAE(){

    jQuery.ajax({
        url: pageInfo.basePath + '/eae/getClinicalMetaDataforEAE',
        type: "POST",
        timeout: '600000',
    }).done(function(serverAnswer) {
        jQuery("#selectedCohort").html(serverAnswer);
    }).fail(function() {
        jQuery("#selectedCohort").html("AJAX CALL FAILED!");
    });
}

