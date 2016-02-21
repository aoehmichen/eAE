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
                        from="${mongoDataTypes}"
                        noSelection="['':'Choose a type of Data']"
                        onchange="displayMongoData()"/>
            </td>
            <td>
                <div id="dataAvailableDiv"></div>
            </td>
        </tr>
    </table>
    <input
            id="showMongoData"
            class='txt'
            type="button"
            value="Show selected data"
            onclick="triggerShowMongoData()"/>
</div>
<br/>


<script>
    var currentWorkflow = "Mongo";

    function displayMongoData(){
        var _t = $('#dataAvailableDiv');
        _t.html("");
        jQuery.ajax({
            url: pageInfo.basePath + '/eae/renderMongoDataList',
            type: "POST",
            timeout: '600000',
            data: {'study': $('#noSQLStudies').val()}
        }).done(function(dataList) {
            _t.append($('<select/>').attr("id", "dataSelect"));
            var _h = $('#dataSelect');
            var dataListJSON= $.parseJSON(dataList);
            $.each(dataListJSON.dataList, function (i, e) {
                _h.append($("<option>")
                        .attr("value",e)
                        .text(e))});
        }).fail(function() {
            _t.html("AJAX CALL FAILED!");
        });
    }

    function triggerShowMongoData() {
        jQuery.ajax({
            url: pageInfo.basePath + '/mongoCache/retieveDataFromMongoFS',
            type: "POST",
            data: {'dataSelected':$('#dataSelect').val()}
        }).done(function(serverAnswer) {
            var jsonAnswer= $.parseJSON(serverAnswer);
            buildOutput(jsonAnswer);
        }).fail(function() {
            jQuery("#eaeoutputs").html("AJAX CALL FAILED!");
        });
    }

    /**
     *   Display the result retieved from the cache
     *   @param jsonRecord
     */
    function buildOutput(jsonRecord){
        var _o = $('#eaeoutputs');
        _o.append($('<img/>').attr("id","mongoImage").attr('src', jsonRecord.dataName));
        _o.append($('<div/>').attr('id', "description").html(jsonRecord.dataDescription));
    }

</script>
