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

    def cacheParams(){
        final String SPARK_URL = grailsApplication.config.com.eae.sparkURL;
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        final String scriptDir = getWebAppFolder() + 'Scripts/eae/';
        final String username = springSecurityService.getPrincipal().username;

        return [SPARK_URL,MONGO_URL,MONGO_PORT,scriptDir,username];
    }

    def runPEForSelectedGenes = {
        final def (SPARK_URL,MONGO_URL,MONGO_PORT,scriptDir,username)= cacheParams();
        String database = "eae";
        String worflow = "pe";
        String saneGenesList = ((String)params.genesList).trim().split(",").sort(Collections.reverseOrder()).join(' ').trim()

        BasicDBObject query = new BasicDBObject("ListOfgenes", saneGenesList);
        // We check if this query has already been made before
        String cached = mongoCacheService.checkIfPresentInCache(MONGO_URL, MONGO_PORT,database, worflow, query)
        def result
        if(cached == "NotCached") {
            String mongoDocumentID = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, database, worflow, username, query)
            String workflowSpecificParameters = params.selectedCorrection
            String dataFileName = worflow + "-" + username + "-" + mongoDocumentID + ".txt" //"listOfGenes.txt"
            eaeDataService.SendToHDFS(username, mongoDocumentID, worflow, saneGenesList, scriptDir, SPARK_URL)
            eaeService.sparkSubmit(scriptDir, SPARK_URL, "pe.py", dataFileName , workflowSpecificParameters, mongoDocumentID)
            result = "Your Job has been submitted. Please come back later for the result"
        }else if (cached == "Completed"){
            result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT,database, worflow, query);
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
        final def (SPARK_URL,MONGO_URL,MONGO_PORT,scriptDir,username)= cacheParams();
        String database = "eae"
        String worflow = "cv";
        println(params);

        def parameterMap = eaeDataService.queryData(params)

        def query = mongoCacheService.buildMongoQuery(params)

        // We check if this query has already been made before
        String cached = mongoCacheService.checkIfPresentInCache((String)MONGO_URL, (String)MONGO_PORT, database, worflow, query)
        def result
        if(cached == "NotCached") {
            String mongoDocumentID = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, database, worflow, username, query)
            String dataFileName = worflow+ "-" + username + "-" + mongoDocumentID + ".txt"
            eaeDataService.SendToHDFS(username, mongoDocumentID, "cv", parameterMap, scriptDir, SPARK_URL)
            eaeService.sparkSubmit(scriptDir, SPARK_URL, "cv.py", dataFileName , workflowSpecificParameters, mongoDocumentID)
            result = "Your Job has been submitted. Please come back later for the result"
        }else if (cached == "Completed"){
            result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT,database, worflow,query);
            mongoCacheService.duplicateCVCacheForUser(MONGO_URL, MONGO_PORT,username, result)
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
