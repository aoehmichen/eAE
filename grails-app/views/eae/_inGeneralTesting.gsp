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
        return true;
    }

    function customWorkflowParameters(){
        var data = [];
        var studySelected = $('#noSQLStudies').val();
        var dataSelected = $('#dataAvailableDiv').val();
        data.push({name: 'studySelected', value: studySelected});
        data.push({name: 'dataSelected', value: dataSelected});
        return data;
    }

    function triggerGT() {
        registerWorkflowParams(currentWorkflow);
        runNoSQLWorkflow();
    }

    function refreshCache(){
        populateCacheDIV(currentWorkflow)
    }

    function cacheDIVCustomName(name){
        var holder =  $('<td/>');
        holder.html(name);
        return holder;
    }
</script>
