<style>
    #controls {
        width: 100%;
        height: 100%;
    }

    .engineButton {
        width: 200px;
        height: 50px;
        background: rgb(65, 130, 195);
        color: white;
        font-size: 14px;
        border: 0 none;
        border-radius: 5px;
        box-shadow: 10px 10px 20px grey;
    }

    .engineButton:hover {
        background: rgb(65, 195, 76);
        cursor: pointer;
    }
</style>

<head>
    <r:layoutResources/>
</head>

<body>
    <div id="index" style="text-align: center">
        <br/>
        <h1 style="font-size: 24px"> Welcome to eTRIKS Analytics!</h1>
        <span style='color:#0200ff; font-size:16px;'>Please select which engine you want to use.</span><br/>
        <br/>
        <div id="controls">
            <g:if test="${grailsApplication.config.com.eae.eaeEnabled}">
                <input class="engineButton" type="button" value="Initialize eAE" onclick="goToEngineDIV('eae')"/>
            </g:if>
        </div>
    </div>
    <r:layoutResources/>
</body>


