<!DOCTYPE html>

<link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
<link rel="stylesheet" href="${resource(dir: 'css', file: 'VolcanoAnalysis.css')}" type="text/css">
<g:javascript src="resource/d3.js"/>
<g:javascript src="smartR/VolcanoAnalysis-compiled.js"/>

<div id="visualization">
    <div id="volcanocontrols" style='float: left; padding-right: 10px'></div>
    <div id="volcanoplot" style='float: left; padding-right: 10px'></div><br/>
    <div id="volcanotable" style='float: left; padding-right: 10px'></div>
</div>

<script>
    builtVolcanoplot(${results});
</script>