<mark>Make sure the project folder in the comparison tab is in the cohort selection.</mark> <br/>
<div id='clinicalData' class="txt">
    This Workflow triggers a generic analysis of the clinical variables.
</div>

<div id='selectStudy' class="txt">
    %{--<script>--}%
        <td style='padding-right: 2em; padding-bottom: 1em'>
            <select id="correctionSelect"/>
            <input type="button" class='txt' onclick="clearVarSelection('studyToAnalyse')" value="Clear Window">
        </td>
        <input
            id="submitCV"
            class='txt'
            type="button"
            value="Run Genereal Testing"
            onclick="triggerGT()"/>
        %{--getClinicalMetaDataforEAE();--}%
    %{--</script>--}%
</div>
<br/>

<hr class="myhr"/>
<div id="cacheTableDiv">
    <table id="mongocachetable" class ="cachetable"></table>
    <div id="emptyCache">The Cache is Empty</div>
    <button type="button"
            value="refreshCacheDiv"
            onclick="refreshCVCache()"
            class="flatbutton">Refresh</button>
</div>

<script>
    var currentWorkflow = "gt";
    populateCacheDIV(currentWorkflow);

    function customSanityCheck() {
        return true;
    }

    function triggerGT() {
        registerWorkflowParams(currentWorkflow);
        runWorkflow();
    }

    function refreshCVCache(){
        populateCacheDIV(currentWorkflow)
    }

    function cacheDIVCustomName(name){
        var holder =  $('<td/>');
        holder.html(name);
        return holder;
    }
</script>
