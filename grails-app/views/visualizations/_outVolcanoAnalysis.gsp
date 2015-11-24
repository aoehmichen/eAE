
<link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
<link rel="stylesheet" href="${resource(dir: 'css', file: 'VolcanoAnalysis.css')}" type="text/css">
<r:require modules="heatmap"/>
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

