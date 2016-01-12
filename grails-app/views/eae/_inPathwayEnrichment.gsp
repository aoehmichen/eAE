
<div id='clinicalData' class="txt">
    This Workflow triggers a pathway enrichment from a list of genes. It uses Fisher's exact test and bonferroni.
</div>

<div id='genesList' class="txt">
    <table id="inputeDataTable">
        <tr>
            <td>
            <form method="post" action="">
                <textarea id="genes" cols="25" rows="5" placeholder="Enter your genes here..."></textarea><br>
                <input
                        id="submitPE"
                        class='txt flatbutton'
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
            onclick="refreshCache()"
            class="flatbutton" >Refresh</button>
</div>

<script>
    var currentWorkflow = "PathwayEnrichment";
    populateCacheDIV(currentWorkflow);

    function customSanityCheck() {
        return true;
    }

    function customWorkflowParameters(){
        var data = [];
        var _s = document.getElementById('correctionSelect');
        var selectedCorrection = _s.options[_s.selectedIndex].value;
        var genesList = $('#genes').val();
        data.push({name: 'studySelected', value: 'PathwayEnrichment'});
        data.push({name: 'dataSelected', value: 'None'});
        data.push({name: 'customField', value: genesList});
        data.push({name: 'workflowSpecificParameters', value: selectedCorrection});
        return data;
    }

    function triggerPE() {
        registerWorkflowParams(currentWorkflow);
        runNoSQLWorkflow();
    }

    function refreshCache(){
        populateCacheDIV(currentWorkflow)
    }

    function cacheDIVCustomName(job){
        var holder =  $('<td/>');
        $.each(job.customfield.split(' '), function (i, e) {
            holder.append(
                    $('<span />').addClass('eae_genetag').text(e)
            )
        });
    return holder;
    }

    function prepareDataForMongoRetrievale(currentworkflow, cacheQuery, workflowspecificparameters) {
        var data = {workflow: currentworkflow, studyname: 'PathwayEnrichment', datatype: 'None', customfield: cacheQuery,
                    workflowspecificparameters: workflowspecificparameters, workflowtype: "NoSQL"};
        return data;
    }

    function buildOutput(jsonRecord){
        var _o = $('#eaeoutputs');
        _o.append($('<table/>').attr("id","TopPathways").attr("class", "cachetable")
                .append($('<tr/>')
                        .append($('<th/>').text("Pathways"))
                        .append($('<th/>').text("Correction: " + jsonRecord.Correction))));
        $.each(jsonRecord.TopPathways, function(i, e){
            $('#TopPathways').append($('<tr/>')
                    .append($('<td/>').text(e[0]))
                    .append($('<td/>').text(e[1])))
        });

        var topPathway = jsonRecord.TopPathways[0][0].toString();
        _o.append($('<br/>').html("&nbsp"));
        _o.append($('<div/>').html(topPathway));
        var html = $.parseHTML(jsonRecord.KeggHTML);
        $.each( html, function( i, el ) {
            if(el.nodeName == "IMG"){
                _o.append($('<img/>').attr('src', "http://www.kegg.jp"+ el.getAttribute("src")));
            }
        });

        _o.append($('<div/>').html(jsonRecord.KeggTopPathway.replace(/\n/g, '<br/>')));
    }

</script>



