package eae.plugin

import com.mongodb.util.JSON
import org.apache.commons.io.FilenameUtils

class EaeController {

    def springSecurityService
    def smartRService
    def eaeDataService
    def eaeService
    def mongoCacheService

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

    def runPEForSelectedGenes = {

        final String SPARK_URL = grailsApplication.config.com.eae.sparkURL;
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        final String scriptDir = grailsApplication.config.com.eae.EAEScriptDir;
        final String username = springSecurityService.getPrincipal().username;

        String saneGenesList = ((String)params.genesList).trim().split(",").sort(Collections.reverseOrder()).join('\t').trim()

        // We check if this query has already been made before
        String cached = mongoCacheService.checkIfPresentInCache(MONGO_URL, MONGO_PORT, "eae", saneGenesList)
        def result
        if(cached == "NotCached") {
            String jobID = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, "eae", "pe", username, saneGenesList)
            String sparkParameters = "pe.py pe_genes.txt Bonferroni " + jobID
            eaeDataService.SendToHDFS(saneGenesList, scriptDir, SPARK_URL)
            println("sent to HDFS")
            eaeService.sparkSubmit(scriptDir, sparkParameters)

            result = "Your Job has been submitted. Please come back later for the result"
        }else if (cached == "Completed"){
            result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT,"eae", saneGenesList)
            println(result)
        }else{
            result = "Your Job has been submitted. Please come back later for the result"
        }

        render template :'/eae/_outPathwayEnrichement', model: [resultPE: result]
    }

    /**
     * Method that will create the get the list of jobs to show in the galaxy jobs tab
     */
    def getjobs = {
        def username = springSecurityService.getPrincipal().username
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        def result = mongoCacheService.getjobs(MONGO_URL, MONGO_PORT, "eae", username, params.workflow)

        response.setContentType("text/json")
        response.outputStream << result as JSON
    }
}
