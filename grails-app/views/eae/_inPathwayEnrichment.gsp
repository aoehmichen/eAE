
<div id='clinicalData' class="txt">
    This Workflow triggers a pathway enrichment from a list of genes. It uses Fisher's exact test and bonferroni.
</div>

<div id='genesList' class="txt">
    <table id="inputeDataTable">
        <tr>
            <td>
            <form method="post" action="">
                <textarea id="genes" cols="25" rows="5">Enter your genes here...</textarea><br>
                <input
                        id="submitPE"
                        class='txt'
                        type="button"
                        value="Run Enrichment"
                        onclick="triggerPE()"/>
            </form>
            </td>
            <td>
                <select id="correctionSelect">
                    <option value="Bonferroni" selected="selected">Bonferroni</option>
                    <option value="HB">Holm-Bonferroni</option>
                    <option value="Sidak">Sidak</option>
                </select>
            </td>
        </tr>
    </table>
</div>
<br/>

<hr class="myhr"/>
<div id="cacheTableDiv">
    <table id="mongocachetable" class ="cachetable"></table>
    <div id="emptyCache">The Cache is Empty</div>
    <button type="button"
            value="refreshCacheDiv"
            onclick="refreshPECache()">Refresh</button>
</div>

<script>
    function triggerPE() {
        var _s = document.getElementById('correctionSelect');
        var selectedCorrection = _s.options[_s.selectedIndex].value;
        runPE(document.getElementById("genes").value, selectedCorrection);
    }

    function refreshPECache(){
        populateCacheDIV("pe")
    }

    populateCacheDIV("pe")

</script>



