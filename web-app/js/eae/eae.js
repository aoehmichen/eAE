
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

function populateCacheDIV(){
    jQuery.ajax({
        url: pageInfo.basePath + '/eae/getjobs',
        type: "POST",
        timeout: '600000'
        }).done(function(serverAnswer) {
        //var _o = $('output')
        //var _t = $('<table/>')
        //$.each([serverAnswer], function (i, e) {
        //    _t.append($('<tr/>').append(
        //        $('<td/>').val(e.nom)
        //    ).append(
        //        $('<td/>').append($('<a/>').attr('href', e.lien).val(e.date))
        //    ).append(
        //        $('<td/>').val(e.heure)
        //    ))
        //})
        //_o.append(_t)

        jQuery("#cacheTable").html(serverAnswer);
    }).fail(function() {
        jQuery("#cacheTable").html("AJAX CALL FAILED!");
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

//function getJobsDataForEAE(workflowSelected)
//{
//    eaejobsstore = new Ext.data.JsonStore({
//        url : pageInfo.basePath+'/eae/getjobs',
//        root : 'jobs',
//        data : {'workflow' : workflowSelected},
//        fields : ['name', 'status', 'startDate']
//    });
//
//    eaejobsstore.on('load', eaejobsstoreLoaded);
//    var myparams = Ext.urlEncode({jobType: 'DataExport',disableCaching: true});
//    eaejobsstore.load({ params : myparams  });
//}
//
//function eaejobsstoreLoaded()
//{
//    var ojobs = Ext.getCmp('ajobsgrid');
//    if(ojobs!=null)
//    {
//        jQuery("#cacheTable").remove(ojobs);
//    }
//    var jobs = new Ext.grid.GridPanel({
//        store: eaejobsstore,
//        id:'ajobsgrid',
//        columns: [
//            {name:'name', header: "Name", width: 120, sortable: true, dataIndex: 'name',
//                renderer: function(value, metaData, record, rowIndex, colIndex, store) {
//                    var changedName;
//                    if (store.getAt(rowIndex).get('status') == 'Completed') {
//                        changedName = '<a href="#">'+value+'</a>';
//                    } else {
//                        changedName = value;
//                    }
//                    return changedName;
//                }
//            },
//            {name:'status', header: "Status", width: 120, sortable: true, dataIndex: 'status'},
//            {name:'startDate', header: "Started On", width: 120, sortable: true, dataIndex: 'startDate'}
//        ],
//        listeners : {cellclick : function (grid, rowIndex, columnIndex, e){
//            var colHeader = grid.getColumnModel().getColumnHeader(columnIndex);
//            if (colHeader == "Name") {
//                var status = grid.getStore().getAt(rowIndex).get('status');
//                if (status == "Error")	{
//                    Ext.Msg.alert("Job Failure", "Unfortunately, an error occurred on this job.");
//                } else if (status == "Cancelled")	{
//                    Ext.Msg.alert("Job Cancelled", "The job has been cancelled");}
//                else if (status == "Completed")	{
//                         // Load the results into outputs from the cache
//                }
//                else if (status != "Completed") {
//                    Ext.Msg.alert("There is something wrong in the status. Unknown status : " + status.toString());
//                }
//            }
//        }
//        },
//        viewConfig:	{
//            forceFit : true
//        },
//        sm : new Ext.grid.RowSelectionModel({singleSelect : true}),
//        layout : 'fit',
//        width : 600,
//        buttons: [{
//            text:'Refresh',
//            handler: function()	{
//                eaejobsstore.reload();
//            }
//        }]
//    });
//    jQuery("#cacheTable").add(jobs);
//    //jQuery("#cacheTable").doLayout();
//}




