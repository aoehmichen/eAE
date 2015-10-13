package eae.plugin

import com.mongodb.BasicDBObject
import grails.util.Environment
import org.apache.commons.io.FilenameUtils
import org.json.JSONObject

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
            render template: '/eae/in' + FilenameUtils.getBaseName(params.script).replaceAll("\\s","")
        }
    }

    def runPEForSelectedGenes = {

        final String SPARK_URL = grailsApplication.config.com.eae.sparkURL;
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        final String scriptDir = getWebAppFolder() + 'Scripts/eae/';
        final String username = springSecurityService.getPrincipal().username;

        String saneGenesList = ((String)params.genesList).trim().split(",").sort(Collections.reverseOrder()).join(' ').trim()

        // We check if this query has already been made before
        String cached = mongoCacheService.checkIfPresentInPECache(MONGO_URL, MONGO_PORT, saneGenesList)
        def result
        if(cached == "NotCached") {
            String mongoDocumentID = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, "eae", "pe", username, saneGenesList)
            String workflowSpecificParameters = params.selectedCorrection
            String dataFileName = "geneList-"+ username + "-" + mongoDocumentID + ".txt" //"listOfGenes.txt"
            eaeDataService.SendToHDFS(username, mongoDocumentID, saneGenesList, scriptDir, SPARK_URL)
            eaeService.sparkSubmit(scriptDir, SPARK_URL, "pe.py", dataFileName , workflowSpecificParameters, mongoDocumentID)
            result = "Your Job has been submitted. Please come back later for the result"
        }else if (cached == "Completed"){
            BasicDBObject query = new BasicDBObject("ListOfgenes", saneGenesList);
            result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT,"eae", "pe",query);
            mongoCacheService.duplicatePECacheForUser(MONGO_URL, MONGO_PORT,username, result)
        }else{
            result = "Your Job has been submitted. Please come back later for the result"
        }
        JSONObject answer = new JSONObject();

        answer.put("iscached", cached);
        answer.put("result", result);

        render answer
    }


    def runCV = {
        final String SPARK_URL = grailsApplication.config.com.eae.sparkURL;
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        final String scriptDir = getWebAppFolder() + 'Scripts/eae/';
        final String username = springSecurityService.getPrincipal().username;

        // We check if this query has already been made before
        String cached = mongoCacheService.checkIfPresentInCVCache(MONGO_URL, MONGO_PORT, saneGenesList)
        def result
        if(cached == "NotCached") {
            String mongoDocumentID = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, "eae", "cv", username, saneGenesList)
            String dataFileName = "CVData-"+ username + "-" + mongoDocumentID + ".txt"
            eaeDataService.SendToHDFS(username, mongoDocumentID, saneGenesList, scriptDir, SPARK_URL)
            eaeService.sparkSubmit(scriptDir, SPARK_URL, "cv.py", dataFileName , workflowSpecificParameters, mongoDocumentID)
            result = "Your Job has been submitted. Please come back later for the result"
        }else if (cached == "Completed"){
            BasicDBObject query = new BasicDBObject("ListOfgenes", saneGenesList);
            result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT,"eae", "cv",query);
            mongoCacheService.duplicatePECacheForUser(MONGO_URL, MONGO_PORT,username, result)
        }else{
            result = "Your Job has been submitted. Please come back later for the result"
        }

        JSONObject answer = new JSONObject();

        answer.put("iscached", cached);
        answer.put("result", result);

        render answer
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
