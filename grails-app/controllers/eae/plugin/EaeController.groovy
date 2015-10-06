package eae.plugin

import grails.util.Environment
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
        final String scriptDir = getWebAppFolder() + 'Scripts/eae/' //grailsApplication.config.com.eae.EAEScriptDir;
        final String username = springSecurityService.getPrincipal().username;

        String saneGenesList = ((String)params.genesList).trim().split(",").sort(Collections.reverseOrder()).join('\t').trim()

        // We check if this query has already been made before
        String cached = mongoCacheService.checkIfPresentInCache(MONGO_URL, MONGO_PORT, "eae", saneGenesList)
        def result
        if(cached == "NotCached") {
            String jobID = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, "eae", "pe", username, saneGenesList)
            String sparkParameters = "pe.py pe_genes.txt Bonferroni " + jobID
            eaeDataService.SendToHDFS(username, saneGenesList, scriptDir, SPARK_URL)
            eaeService.sparkSubmit(scriptDir, sparkParameters)

            result = "Your Job has been submitted. Please come back later for the result"
        }else if (cached == "Completed"){
            result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT,"eae", saneGenesList)
            println(result)
        }else{
            result = "Your Job has been submitted. Please come back later for the result"
        }

        render template :'/eae/outPathwayEnrichment', model: [resultPE: result]
    }

    /**
     * Method that will create the get the list of jobs to show in the galaxy jobs tab
     */
    def retieveCachedJobs = {
        def username = springSecurityService.getPrincipal().username
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        def workflow = ""


        if(params.script == null) {
        workflow = "pe"
        }else {

            switch (params.script) {
                case "Pathway Enrichment":
                    workflow = "pe";
                    break;
                case "General Testing":
                    workflow = "gt";
                    break;
                case "Cross Validation":
                    workflow = "cv";
                    break;
                case "Label Propagation":
                    workflow = "lp";
                    break;
                default:
                    throw new Exception("The workflow doesn't exist.")
            }
        }
        def result = mongoCacheService.getjobsFromMongo(MONGO_URL, MONGO_PORT, "eae", username, workflow)

        if(result.get("totalCount") == 0){
            render "The Cache is empty"}
        else{
            render result
        }
    }


    /**
     *   Gets the directory where all the R scripts are located
     *
     *   @return {str}: path to the script folder
     */
    def getWebAppFolder() {
        if (Environment.current == Environment.DEVELOPMENT) {
            return org.codehaus.groovy.grails.plugins.GrailsPluginUtils
                    .getPluginDirForName('smart-r')
                    .getFile()
                    .absolutePath + '/web-app/'
        } else {
            return grailsApplication
                    .mainContext
                    .servletContext
                    .getRealPath('/plugins/') + '/smart-r-0.1/'
        }
    }
}
