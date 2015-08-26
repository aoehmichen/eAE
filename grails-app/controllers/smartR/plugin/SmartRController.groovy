package smartR.plugin

import grails.converters.JSON
import org.apache.commons.io.FilenameUtils

class SmartRController {

    def smartRService
    def eaeService

    /**
     *   Renders the actual visualization based on the chosen script and the results computed
     */
    def renderOutputDIV = {
        params.init = params.init == null ? true : params.init // defaults to true
        def (success, results) = smartRService.runJob(params)
        if (!success) {
            render results
        } else {
            render template: '/visualizations/' + 'out' + FilenameUtils.getBaseName(params.script),
                    model: [results: results as JSON]
        }
    }

    def updateOutputDIV = {
        params.init = false
        def (success, results) = smartRService.runJob(params)
        def answer = success ? results : [error: [results]]
        render answer as JSON
    }

    def recomputeOutputDIV = {
        params.init = false
        redirect controller: 'SmartR',
                action: 'renderOutputDIV',
                params: params
    }

    /**
     *   Renders the input form for initial script parameters
     */
    def renderInputDIV = {
        if (!params.script) {
            render 'Please select a script to execute.'
        } else {
            render template: '/smartR/' + 'in' + FilenameUtils.getBaseName(params.script)
        }
    }

    /**
     *   Go to eTRIKS Analytical Engine
     */
    def goToEAEngine = {
        render template: '/eae/home', model:[ hpcScriptList: eaeService.hpcScriptList] }

}