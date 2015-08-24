package eae.plugin

import grails.converters.JSON
import org.apache.commons.io.FilenameUtils

class EaeController {

    def smartRService
    def EaeDataService


    /**
     *   Go to SmartR
     */
    def goToSmartR = {
        render template: '/smartR/index', model:[ scriptList: smartRService.scriptList] }

    /**
     *   Renders the input form for initial script parameters
     */
    def renderInputs = {
        if (! params.script) {
            render 'Please select a script to execute.'
        } else {
            render template: '/eae/' + 'in' + FilenameUtils.getBaseName(params.script).replaceAll("\\s","")
        }
    }

    def getClinicalMetaDataforEAE = {
        List<Long> resultInstanceIds = parseResultInstanceIds()

        render EaeDataService.getClinicalMetaDataForEAE(
                resultInstanceIds[0],
                resultInstanceIds[1]) as JSON
    }


    private List<Long> parseResultInstanceIds () {
        List<Long> result = []
        int subsetNumber = 1
        while (params.containsKey('result_instance_id' + subsetNumber)) {
            result << params.long('result_instance_id' + subsetNumber)
            subsetNumber += 1
        }
        result
    }

    def runPEForSelectedGenes = {

        println(params)
        render params.genesList.toString()
    }
}
