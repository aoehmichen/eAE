
<head>
<g:javascript library='jquery' />
<g:javascript src='eae/eae.js' />
<link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
<link href='<g:resource dir="css" file="eae.css" />' rel='stylesheet' type='text/css'>
<r:layoutResources/>
    <style>
    .txt {
        font-family: 'Roboto', sans-serif;
    }
    </style>
</head>

<body>
    <div id="switch" style="text-align: right">
    <input
        id="goSmartR"
        class='txt'
        type="button"
        value="Go To SmartR"
        onclick="goToSmartRScript()"/>
    </div>
    <div id="index" style="text-align: center">
        <h1 class="txt"> Welcome to eTRIKS Analytical Engine.</h1>
        <span style='color:red' class='txt'>Welcome to the danger zone!! https://www.youtube.com/watch?v=kyAn3fSs8_A </span><br/>
        <hr class="myhr"/>

        <div id="eaeinputs" class='txt' style="text-align: left">Please select a script to execute.</div>

        <hr class="myhr"/>
        <g:select
        name="hpcscriptSelect"
        class='txt'
        from="${hpcScriptList}"
        noSelection="['':'Choose an algorithm']"
        onchange="changeEAEInput()"/>
    &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp

    <hr class="myhr"/>
    </div>

    <div id="eaeoutputs" ></div>
</body>

<script>


</script>