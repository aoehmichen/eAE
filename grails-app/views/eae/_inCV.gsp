<mark>Step:</mark> Drop a High dimensional variable into this window.<br/>

<div id='description' class="txt">
    This Workflow triggers a cross validation workflow coupled with a model builder algrorithm.
</div>

<div id='geneExp' class="txt">
    <form method="post" action="">
        <td style='padding-right: 2em; padding-bottom: 1em'>
            <div id='highDimData' class="queryGroupIncludeSmall"></div>
            <input type="button" class='txt' onclick="clearVarSelection('highDimData')" value="Clear Window">
        </td>
        <input
                id="submitCV"
                class='txt'
                type="button"
                value="Run CV"
                onclick="triggerCV()"/>
    </form>
</div>
<br/>

<hr class="myhr"/>
<div id="cacheTableDiv">
    <table id="mongocachetable" class ="cachetable"></table>
    <div id="emptyCache">The Cache is Empty</div>
    <button type="button"
            value="refreshCacheDiv"
            onclick="refreshCVCache()">Refresh</button>
</div>

<script>
    activateDragAndDropEAE('highDimData');

    registerConceptBoxEAE('highDimData', 1, 'alphaicon', 0, undefined);

    function triggerCV() {
        runCV()
    }

    function refreshCVCache(){
        populateCacheDIV("cv")
    }

    populateCacheDIV("cv")
</script>