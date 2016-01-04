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
                    %{--<g:each in="${noSQLStudies}" var="study">--}%
                        %{--<option value="${study}">${study}</option>--}%
                    %{--</g:each>--}%
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
        var _s = document.getElementById('correctionSelect');
        var selectedCorrection = _s.options[_s.selectedIndex].value;
        var genesList = $('#genes').value;
        data.push({name: 'genesList', value: genesList});
        data.push({name: 'selectedCorrection', value: selectedCorrection});
        return data;
    }

    function triggerGT() {
        registerWorkflowParams(currentWorkflow);
        runNoSQLWorkflow(mongoData);
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
