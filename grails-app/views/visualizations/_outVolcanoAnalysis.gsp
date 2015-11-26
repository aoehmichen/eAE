<r:require modules="volcano_analysis"/>
<r:layoutResources/>

<div id="visualization">
    <div id="volcanocontrols" style='float: left; padding-right: 10px'></div>
    <div id="volcanoplot" style='float: left; padding-right: 10px'></div><br/>
    <div id="volcanotable" style='float: left; padding-right: 10px'></div>
</div>
<r:layoutResources/>

<script>
    buildVolcanoAnalysis(${results})
</script>

