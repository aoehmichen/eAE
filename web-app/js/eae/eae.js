
/**
 *   Activating drag and drop for a given div
 *
 *   @param {string} divName: name of the div element to activate drag and drop for
 */
function activateDragAndDropEAE(divName) {
    //console.log('Activating drag and drop')
    var div = Ext.get(divName);
    var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
    dtgI.notifyDrop = dropOntoCategorySelection;
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
function registerConceptBoxEAE(name, cohorts, type, min, max) {
    //console.log('registering concept')
    var concepts = getConcepts(name);
    console.log('Hello: ', concepts)
    var check1 = type === undefined || containsOnly(name, type);
    var check2 = min === undefined || concepts.length >= min;
    var check3 = max === undefined || concepts.length <= max;
    sanityCheckErrors.push(
        !check1 ? 'Concept box (' + name + ') contains concepts with invalid type! Valid type: ' + type :
            !check2 ? 'Concept box (' + name + ') contains too few concepts! Valid range: ' + min + ' - ' + max :
                !check3 ? 'Concept box (' + name + ') contains too many concepts! Valid range: ' + min + ' - ' + max : '');
    conceptBoxes.push({name: name, cohorts: cohorts, type: type, concepts: concepts});
}

/**
 *   Prepares data for the AJAX call containing all neccesary information for computation
 *
 *   @return {[]}: array of objects containing the information for server side computations
 */
function prepareFormData() {
    var data = [];
    data.push({name: 'conceptBoxes', value: JSON.stringify(conceptBoxes)});
    data.push({name: 'result_instance_id1', value: GLOBAL.CurrentSubsetIDs[1]});
    data.push({name: 'result_instance_id2', value: GLOBAL.CurrentSubsetIDs[2]});
    data.push({name: 'settings', value: JSON.stringify(getSettings())});
    console.log(JSON.stringify(getSettings()));
    return data;
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
    jQuery("#eaeoutputs").html("");

    jQuery.ajax({
        url: pageInfo.basePath + '/eae/renderInputs',
        type: "POST",
        timeout: '600000',
        data: {'script': jQuery('#hpcscriptSelect').val()}
    }).done(function(serverAnswer) {
        jQuery("#eaeinputs").html(serverAnswer);
    }).fail(function() {
        jQuery("#eaeinputs").html("AJAX CALL FAILED!");
    });

}

/**
 * This function pulls all the available cache records for the user for the select workflow
 * @param currentworkflow - from the available eae workflows
 */
function populateCacheDIV(currentworkflow){
    var _t = $('#mongocachetable');
    _t.empty();
    _t.append($('<tr/>').attr("id", "headersRow"));

    var cacheTableHeaders = ["Name", "Date", "Status", "Cached Results"];
    var _h = $('#headersRow');
    $.each(cacheTableHeaders, function(i, e){
        _h.append($('<th/>').text(e))
    });

    jQuery.ajax({
        url: pageInfo.basePath + '/mongoCache/retrieveCachedJobs',
        type: "POST",
        data:{workflow: currentworkflow}
        }).done(function(cachedJobs) {
        var jsonCache= $.parseJSON(cachedJobs);

        if(jsonCache.totalCount == 0){
            jQuery("#mongocachetable").hide();
            jQuery("#emptyCache").show();
        }else {

            var date;
            jQuery("#mongocachetable").show();
            jQuery("#emptyCache").hide();
            $.each(jsonCache.jobs, function (i, e) {
                date = new Date(e.start.$date);
                _t.append($('<tr/>').append(
                    $('<td/>').text(e.name)
                ).append(
                    $('<td/>').text(e.status)
                ).append(
                    $('<td/>').text(date)
                ).append(
                     $('<td/>').append($('<button/>').attr('data-button', e.name).on('click',function(){
                        var cacheQuery= $(this).attr('data-button');
                         showWorkflowOutput(currentworkflow,cacheQuery);
                     }).text("Result"))
                ))
            })
        }
    }).fail(function() {
        jQuery("#cacheTableDiv").html("AJAX CALL FAILED!");
    });
}

/**
 *
 * @param currentworkflow
 * @param cacheQuery
 */
function showWorkflowOutput(currentworkflow, cacheQuery){
    jQuery("#eaeoutputs").html("");
    jQuery.ajax({
        url: pageInfo.basePath + '/mongoCache/retrieveSingleCachedJob',
        type: "POST",
        data: {cacheQuery: cacheQuery, workflow: currentworkflow}
    }).done(function(cachedJob) {
        var jsonRecord= $.parseJSON(cachedJob);
        switch (currentworkflow){
            case "pe":
                buildPEOutput(jsonRecord)
                break;
            case "cv":
                buildCVOutput(jsonRecord);
                break;
            default:
                console.log("The workflow selected:" + currentworkflow.toString() + " doesn't exist.")
        }
    })
}

function buildPEOutput(jsonRecord){
    var _o = $('#eaeoutputs');
    _o.append($('<table/>').attr("id","topPathways").attr("class", "cachetable")
        .append($('<tr/>')
            .append($('<th/>').text("Pathways"))
            .append($('<th/>').text("Correction: " + jsonRecord.Correction))));
    $.each(jsonRecord.topPathways, function(i, e){
        $('#topPathways').append($('<tr/>')
            .append($('<td/>').text(e[0]))
            .append($('<td/>').text(e[1])))
    });

    var topPathway = jsonRecord.topPathways[0][0].toString();
    _o.append($('<div/>').html(topPathway));
    _o.append($('<img/>').attr('src', "http://rest.kegg.jp/get/"+ topPathway +"/image"));
    _o.append($('<div/>').text(jsonRecord.KeggTopPathway));
}

/**
*   Display the result retieved from the cache
*   @param cacheQuery
*/
function buildCVOutput(cacheQuery){
    var _o = $('#eaeoutputs');
    // TODO
    console.log("TODO buildCVOutput")
}


/**
 *   Run a pathway enrichment from the eae
 */
function runPE(list, selectedCorrection){
    jQuery.ajax({
        url: pageInfo.basePath + '/eae/runPEForSelectedGenes',
        type: "POST",
        data: {'genesList': list, 'selectedCorrection': selectedCorrection}
    }).done(function(serverAnswer) {
        var jsonAnswer= $.parseJSON(serverAnswer);

        if(jsonAnswer.iscached === "NotCached"){
            jQuery("#eaeoutputs").html(jsonAnswer.result);
        }else{
            buildPEOutput(jsonAnswer.result);
            //jQuery("#eaeoutputs").html("Cached but viz to do");
        }
    }).fail(function() {
        jQuery("#eaeoutputs").html("AJAX CALL FAILED!");
    });
}


/**
 *   get the input from datasetexplorer
 */
function getClinicalMetaDataforEAE(){

    jQuery.ajax({
        url: pageInfo.basePath + '/eae/getClinicalMetaDataforEAE',
        type: "POST"
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




