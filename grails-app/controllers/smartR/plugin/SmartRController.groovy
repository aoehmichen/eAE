package smartR.plugin

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.apache.commons.io.FilenameUtils

class SmartRController {

    def smartRService
    def sessionManagerService
    def eaeService

    def getState = {
        render sessionManagerService.getState(params.id)
    }

    def getMsg = {
        render sessionManagerService.getMsg(params.id)
    }

    def initSession = {
        sessionManagerService.initSession(params.id, params.init.toBoolean())
        render ''
    }

    def loadDataIntoSession = {
        sessionManagerService.setState(params.id, 1)
        if (params.init.toBoolean()) {
            def data = smartRService.queryData(params.rIID1, params.rIID2, new JsonSlurper().parseText(params.conceptBoxes))
            sessionManagerService.pushData(params.id, data)
        }
        sessionManagerService.pushSettings(params.id, params.settings)
        render ''
    }

    def runWorkflowScript = {
        sessionManagerService.setState(params.id, 3)
        sessionManagerService.runWorkflowScript(params.id, smartRService.getWebAppFolder() + '/Scripts/smartR/' + params.script)
        render ''
    }

    def renderResults = {
        def results = sessionManagerService.pullData(params.id)
        if (params.redraw.toBoolean()) {
            render template: "/visualizations/out${FilenameUtils.getBaseName(params.script)}",
                    model: [results: results]
        } else {
            render results
        }
    }

    def renderInputDIV = {
        if (! params.script) {
            render 'Please select a script to execute.'
        } else {
            render template: "/smartR/in${FilenameUtils.getBaseName(params.script)}"
        }
    }

    def renderLoadingScreen = {
        render template: "/visualizations/outLoading"
    }

    def goToEAEngine = {
        render template: '/eae/home', model:[ hpcScriptList: eaeService.hpcScriptList]
    }

    def loadScripts = {
        JSONObject result = new JSONObject()
        JSONObject script = new JSONObject()
        script.put("path", "${servletContext.contextPath}${pluginContextPath}/js/etriksEngines/engineSelection.js" as String)
        script.put("type", "script")
        result.put("success", true)
        result.put("files", new JSONArray() << script)
        render result as JSON;
    }
}
