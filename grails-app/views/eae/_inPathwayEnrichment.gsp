
<div id='clinicalData' class="txt">
    This Workflow triggers a pathway enrichment from a list of genes. It uses Fishe's exact test and bonferroni.
</div>

<div id='genesList' class="txt">
    <form method="post" action=runPE()>
        <textarea name="genes" cols="25" rows="5">Enter your genes here...
        </textarea><br>
        <input
                id="submitPE"
                class='txt'
                type="button"
                value="Run Enrichment"
                onclick="runPE()"/>
    </form>
</div>
<br/>

<script>


</script>



