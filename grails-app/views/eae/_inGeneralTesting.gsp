<div id='clinicalData' class="txt">
    This Workflow triggers a generic analysis of the clinical variables.
</div>

<div id='selectStudy' class="txt">
    <table>
        <tr>
            <td>
                <g:select
                        name="noSQLStudies"
                        class='txt'
                        from="${noSQLStudies}"
                        noSelection="['':'Choose a study']"
                        onchange="displayDataForStudy()"/>
            </td>
            <td>
                <div id="dataAvailableDiv"></div>
            </td>
        </tr>
    </table>
    <br/>
    <span>Confidence for the normality test:</span>
    <select id='confidenceNormality'>
        <option value="15.0">15.0</option>
        <option value="10.0">10.0</option>
        <option value="5.0">5.0</option>
        <option value="2.5">2.5</option>
        <option value="1.0">1.0</option>
    </select>
    <br/>
    <br/>
    <input
            id="submitGT"
            class='txt'
            type="button"
            value="Run Genereal Testing"
            onclick="triggerGT()"/>
</div>
<br/>

<hr class="myhr"/>
<div id="cacheTableDiv">
    <table id="mongocachetable" class="cachetable"></table>
    <div id="emptyCache">The Cache is Empty</div>
    <button type="button"
            value="refreshCacheDiv"
            onclick="refreshCache()"
            class="flatbutton">Refresh</button>
</div>

<script>
    var currentWorkflow = "GeneralTesting";
    populateCacheDIV(currentWorkflow);

    function customSanityCheck() {
        var studySelected = $('#noSQLStudies').val();
        var dataSelected = $('#dataTypeSelect').val();
        if(studySelected === undefined || dataSelected === undefined){
            alert("Please select a study and data to be used.");
            return false;
        }
        return true;
    }

    function customWorkflowParameters(){
        var data = [];
        var studySelected = $('#noSQLStudies').val();
        var dataSelected = $('#dataTypeSelect').val();
        var confidenceNormality = $('#confidenceNormality').val();
        data.push({name: 'studySelected', value: studySelected});
        data.push({name: 'dataSelected', value: dataSelected});
        data.push({name: 'customField', value: 'None'});
        data.push({name: 'workflowSpecificParameters', value: confidenceNormality + " " + studySelected + " " + dataSelected});
        return data;
    }

    function triggerGT() {
        registerWorkflowParams(currentWorkflow);
        runNoSQLWorkflow();
    }

    function refreshCache(){
        populateCacheDIV(currentWorkflow);
    }

    function cacheDIVCustomName(job){
        var name = "Study Selected : " + job.studyname + "\<br /> Data Selected : " + job.datatype ;
        var holder =  $('<td/>');
        holder.html(name);
        return {
            holder: holder,
            name: name
        };
    }

    /**
     *   Display the result retieved from the cache
     *   @param jsonRecord
     */
    function buildOutput(jsonRecord){
        var _o = $('#eaeoutputs');

        _o.append($('<table/>').attr("id","gttable").append($('<tr/>')
                        .append($('<th/>').text("Name :"))
                        .append($('<th/>').text(jsonRecord.PearsonCorrelationHeatmapName))
        ));
        $('#gttable').append($('<tr/>')
                        .append($('<td/>').append($('<div/>').attr('id',"imageerror")))
                        .append($('<td/>').append($('<img/>').attr("id","correlationHeatmap")))
        );


        jQuery.ajax({
            url: pageInfo.basePath + '/mongoCache/retieveDataFromMongoFS',
            type: "POST",
            timeout: '600000',
            data: {'FileName': jsonRecord.PearsonCorrelationHeatmapName}
        }).done(function(serverAnswer) {
            $('#correlationHeatmap').attr("src", serverAnswer);
        }).fail(function() {
            $('#correlationHeatmap').html("Cannot get the Image!")
        });

//        _o.append($('<div/>').attr('id', "cvPerformanceGraph"));
        // d3.select('#cvPerformanceGraph').datum(formatData(jsonRecord.PerformanceCurve)).call(chart);
    }

    function prepareDataForMongoRetrievale(currentworkflow, cacheQuery, workflowspecificparameters) {
        var tmpData = [];
        var splitTerms = cacheQuery.split('<br />');
        $.each(splitTerms, function (i, e) {
            var chunk = e.split(':');
            tmpData.push(chunk[1].trim());
        });
        var data = {
            Workflow: currentworkflow,
            StudyName: tmpData[0],
            DataType: tmpData[1],
            CustomField: 'None',
            WorkflowSpecificParameters: workflowspecificparameters
        };
        return data;
    }

</script>
