package eae.plugin

import grails.util.Environment
import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.web.json.JSONObject

class EaeController {

    def springSecurityService
    def smartRService
    def eaeDataService
    def eaeNoSQLDataService
    def eaeService
    def mongoCacheService

    def cacheParams(){
        final String SPARK_URL = grailsApplication.config.com.eae.interfaceEAEURL;
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        final String scriptDir = getWebAppFolder() + 'Scripts/eae/';
        final String username = springSecurityService.getPrincipal().username;

        return [SPARK_URL,MONGO_URL,MONGO_PORT,scriptDir,username];
    }

    def noSQLParams(){
        final String MONGO_URL = grailsApplication.config.com.eae.nosqlURL;
        final String targetDB = "studies";
        return [MONGO_URL, targetDB];
    }

    def oozieParams(){
        final String OOZIE_URL = grailsApplication.config.com.eae.OOzieURL; //"http://146.169.32.200:11000/oozie";
        final String JOB_TRACKER = grailsApplication.config.com.eae.jobTracker; //"eti-spark-master.novalocal";
        final String JOB_TRACKER_PORT = grailsApplication.config.com.eae.jobTrackerPort;// "8032";
        final String NAMENODE = grailsApplication.config.com.eae.hdfsNamenode; //"eti-spark-master.novalocal";
        final String NAMENODE_PORT =grailsApplication.config.com.eae.hdfsNamenodePort; //"8020"

        return [OOZIE_URL, JOB_TRACKER, JOB_TRACKER_PORT, NAMENODE, NAMENODE_PORT];
    }

    def interfaceParams(){
        final String INTERFACE_URL = grailsApplication.config.com.eae.interfaceEAEURL;
        return INTERFACE_URL
    }

    /**
     *   Go to SmartR
     */
    def goToSmartR = {
        render template: '/smartR/index', model:[ scriptList: smartRService.scriptList] }


    /**
     *   Renders the input form for initial script parameters.
     */
    def renderInputs = {
        if (! params.workflow) {
            render 'Please select a script to execute.'
        } else {
            String workflowSelected = params.workflow;
            final def (NOSQL_URL, database) = noSQLParams();
            if(workflowSelected == "Mongo"){
                render template: '/eae/in' + FilenameUtils.getBaseName(params.workflow).replaceAll("\\s", ""),
                        model: [mongoDataTypes: eaeNoSQLDataService.getMongoData(NOSQL_URL, database)]
            }else {
                render template: '/eae/in' + FilenameUtils.getBaseName(params.workflow).replaceAll("\\s", ""),
                        model: [noSQLStudies: eaeNoSQLDataService.getStudies(NOSQL_URL, database, workflowSelected)]
            }
        }
    }

    /**
     * Sends back the list of available High dimensional data for the selected study.
     *
     * @return {list(str)}
     */
    def renderDataList = {
        final def (NOSQL_URL, database) = noSQLParams();
        def listOfData = eaeNoSQLDataService.getDataTypesForStudy(NOSQL_URL, database, params.study);
        render listOfData;
    }

    /**
     * Workflows which are using data coming from Mongo trigger this function.
     *
     * @return {json}: returns the result if it was cached or a message if it wasn't.
     */
    def runNoSQLWorkflow = {
        final def (SPARK_URL,MONGO_CACHE_URL,MONGO_CACHE_PORT,scriptDir,username)= cacheParams();
        // final String NOSQL_URL, database = noSQLParams();
        String database = "eae";
        final def INTERFACE_URL = interfaceParams();
        String workflow = params.workflow;

        // We start building the mongo cache query and check if the workflow has already been run before
        def query = mongoCacheService.buildMongoCacheQueryNoSQL(params);
        String cached = mongoCacheService.checkIfPresentInCache(MONGO_CACHE_URL, MONGO_CACHE_PORT,database, workflow, query)

        // We check if this query has already been made before
        def result
        def workflowParameters = [:]
        if(cached == "NotCached") {
            String mongoDocumentID = mongoCacheService.initJob(MONGO_CACHE_URL, MONGO_CACHE_PORT, database, workflow, "NoSQL", username, query)
            workflowParameters['workflow'] = workflow;
            workflowParameters['workflowType'] = "NoSQL";
            workflowParameters['workflowSpecificParameters'] = params.workflowSpecificParameters;
            workflowParameters['mongoDocumentID'] = mongoDocumentID;
            workflowParameters['zipFile'] = "";
            def status = eaeService.eaeInterfaceSparkSubmit(INTERFACE_URL,workflowParameters);
            result = "Your Job has been submitted. Please come back later for the result"
        }else if (cached == "Completed"){
            result = mongoCacheService.retrieveValueFromCache(MONGO_CACHE_URL, MONGO_CACHE_PORT,database, workflow, query);
            query.append("User", username);
            query.removeField("DocumentType");
            query.append("DocumentType", "Copy")
            Boolean copyAlreadyExists = mongoCacheService.copyPresentInCache(MONGO_CACHE_URL, MONGO_CACHE_PORT,database, workflow, query);
            if(!copyAlreadyExists) {
                mongoCacheService.duplicateCacheForUser(MONGO_CACHE_URL, MONGO_CACHE_PORT, database, workflow, username, result);
            }
            result = eaeService.customPostProcessing(result, params.workflow)
        }else{
            result = "The job requested has been submitted by another user and is now computing. Please try again later for the result."
        }
        JSONObject answer = new JSONObject();

        answer.put("iscached", cached);
        answer.put("result", result);

        render answer
    }

    /**
     * Workflows which are using data coming from tranSMART trigger this function.
     *
     * @return {json}: returns the result if it was cached or a message if it wasn't.
     */
    def runWorkflow = {
        final def (SPARK_URL,MONGO_CACHE_URL,MONGO_CACHE_PORT,scriptDir,username)= cacheParams();
        //final def (OOZIE_URL, JOB_TRACKER, JOB_TRACKER_PORT, NAMENODE, NAMENODE_PORT) = oozieParams();
        final def INTERFACE_URL = interfaceParams();
        String database = "eae";
        String workflow = params.workflow;

        // We start building the mongo cache query and check if the workflow has already been run before
        def parameterMap = eaeDataService.queryData(params);
        def query = mongoCacheService.buildMongoCacheQuery(params,parameterMap);

        // We check if this query has already been made before
        String cached = mongoCacheService.checkIfPresentInCache((String)MONGO_CACHE_URL, (String)MONGO_CACHE_PORT, database, workflow, query)
        def result
        if(cached == "NotCached") {
            // The pre processing step is required to add default parameters that are not available in the UI to set but required by the workflow.
            // this could be removed in the eventuallity all parameters can be set in the workflow UI for all workflows.
            def workflowParameters = eaeService.customPreProcessing(params, workflow, MONGO_CACHE_URL, MONGO_CACHE_PORT, database, username)

            String mongoDocumentID = mongoCacheService.initJob(MONGO_CACHE_URL, MONGO_CACHE_PORT, database, workflow, "SQL", username, query)

            // Transfer the data file and additional file to HDFS
            String dataFileName = eaeDataService.sendToHDFS(username, mongoDocumentID, workflow, parameterMap, scriptDir, SPARK_URL, "data")
            String additionalFileName = eaeDataService.sendToHDFS(username, mongoDocumentID, workflow, parameterMap, scriptDir, SPARK_URL, "additional")
            String zipFileName = workflow + "-" + username + "-" + mongoDocumentID;

            eaeDataService.zipFiles([dataFileName,additionalFileName], zipFileName)

            workflowParameters['mongoDocumentID'] = mongoDocumentID;
            workflowParameters['workflowType'] = "SQL";
            workflowParameters['zipFile'] = zipFileName;
            workflowParameters['workflowSpecificParameters'] = "";
//            workflowParameters['dataFileName'] = dataFileName;
//            workflowParameters['additionalFileName'] = additionalFileName;
            eaeService.eaeInterfaceSparkSubmit(INTERFACE_URL, workflowParameters);
            //eaeService.scheduleOOzieJob(OOZIE_URL, JOB_TRACKER, JOB_TRACKER_PORT, NAMENODE, NAMENODE_PORT, workflow, workflowParameters);
            //eaeService.sparkSubmit(scriptDir, SPARK_URL, workflow+".py", dataFileName , workflowSpecificParameters, mongoDocumentID)
            result = "Your Job has been submitted. Please come back later for the result"

        }else if (cached == "Completed"){
            // The result is already available and we send back the result right away.
            result = mongoCacheService.retrieveValueFromCache(MONGO_CACHE_URL, MONGO_CACHE_PORT, database, workflow, query);
            query.append("User", username);
            query.removeField("DocumentType");
            query.append("DocumentType", "Copy")
            Boolean copyAlreadyExists = mongoCacheService.copyPresentInCache(MONGO_CACHE_URL, MONGO_CACHE_PORT,database, workflow, query);
            if(!copyAlreadyExists) {
                // We create a copy to be listed in the mongo cache list of the workflow for the user.
                mongoCacheService.duplicateCacheForUser(MONGO_CACHE_URL, MONGO_CACHE_PORT, database, workflow, username, result);
            }
            result = eaeService.customPostProcessing(result, params.workflow)
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
        def eaeFileSystemName = applicationContext.getBean('pluginManager').allPlugins.sort({ it.name.toUpperCase() }).find { it.fileSystemName ==~ /eae-\w.\w/}
        if (Environment.current == Environment.DEVELOPMENT) {
            return org.codehaus.groovy.grails.plugins.GrailsPluginUtils
                    .getPluginDirForName('eae')
                    .getFile()
                    .absolutePath + '/web-app/'
        } else {
            return grailsApplication
                    .mainContext
                    .servletContext
                    .getRealPath('/plugins/') + '/'+ eaeFileSystemName.fileSystemName.toString() + '/'
        }
    }
}
