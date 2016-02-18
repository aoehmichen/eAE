package eae.plugin
import com.mongodb.BasicDBObject
import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import groovyx.net.http.AsyncHTTPBuilder
import org.apache.oozie.client.OozieClient
import org.json.JSONObject
import org.springframework.web.context.request.RequestContextHolder

import static groovyx.net.http.ContentType.HTML
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET

@Transactional
class EaeService {

    def mongoCacheService

    /**
     *   Renders the default view
     */
    def getHpcScriptList() {
        def scriptList = ['Cross Validation', 'Pathway Enrichment', 'General Testing']
        return scriptList
    }

    def scheduleOOzieJob(String OOZIE_URL, String JOB_TRACKER, String JOB_TRACKER_PORT, String NAMENODE, String NAMENODE_PORT, String workflow, workflowParameters){
        // get a OozieClient for local Oozie
        OozieClient wc = new OozieClient(OOZIE_URL);

        // create a workflow job configuration and set the workflow application path
        Properties conf = wc.createConfiguration();
        conf.setProperty(OozieClient.USER_NAME, "ubuntu");
        conf.setProperty("jobTracker", JOB_TRACKER + ":" + JOB_TRACKER_PORT); // the port must match yarn.resourcemanager.address's
        conf.setProperty("nameNode", "hdfs://" + NAMENODE + ":" + NAMENODE_PORT); //this is for the regular install but mapr needs maprfs ....
        conf.setProperty(OozieClient.APP_PATH, "hdfs://"+ NAMENODE + ":" + NAMENODE_PORT + "/user/ubuntu/workflows/" + workflow +"_workflow.xml");

        // setting workflow parameters
        workflowParameters.each{
            k, v -> conf.setProperty(k,v) }

        // submit and start the workflow job
        String jobId = wc.run(conf);

        return jobId;
    }
//
//    def sparkSubmit(String scriptDir, String SparkURL, String workflowFileName, String dataFileName, String workflowSpecificParameters, String mongoDocumentID){
//        def script = scriptDir +'executeSparkJob.sh'
//
//        def scriptFile = new File(script)
//        if (scriptFile.exists()) {
//            if (!scriptFile.canExecute()) {
//                scriptFile.setExecutable(true)
//            }
//        }else {
//            log.error('The Script file spark submit wasn\'t found')
//        }
//        def executeCommand = script + " " + SparkURL + " " + workflowFileName + " " + dataFileName + " " + workflowSpecificParameters + " " + mongoDocumentID
//        println(executeCommand)
//        executeCommand.execute().waitFor()
//        return 0
//    }

    def customPreProcessing(params, workflow, MONGO_URL,MONGO_PORT,database,username){
        switch (workflow){
            case "cv":
                return cvPreprocessing(params, MONGO_URL,MONGO_PORT,database,username);
            default:
                throw new Exception("The workflow in customPreProcessing doesn't exist.")
        }
    }

    private def cvPreprocessing(params, MONGO_URL,MONGO_PORT,database,username){
        def workflowParameters = [:];
        String mongoDocumentIDPE = "abcd0000" ;// fake mongoId
        String doEnrichement = "false";
        String algorithmToUse = "SVM"
        String kfold= "0.2";
        String resampling = "1";
        String numberOfFeaturesToRemove = "0.4"

        if( params.doEnrichment){
            def query = new BasicDBObject("StudyName" , "PathwayEnrichment")
            query.append("DataType","CrossValidation")
            query.append("CustomField","")
            query.append("WorkflowSpecificParameters","Bonferroni")
            mongoDocumentIDPE = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, database, "PathwayEnrichement", "NoSQL", username, query )
            doEnrichement = "true"
        }

        workflowParameters['workflow'] = params.workflow;
        workflowParameters['workflowSpecificParameters'] = algorithmToUse + " " + kfold + " " + resampling + " " +  numberOfFeaturesToRemove + " " + doEnrichement + " " + mongoDocumentIDPE.toString();

        return workflowParameters;
    }

    def customPostProcessing(result, workflow) {
        switch (workflow){
            case "PathwayEnrichment":
                return pePostProcessing(result);
            default:
                return result
        }
    }

    private def pePostProcessing(result) {
        def topPathway = result.get('TopPathways').get(0).get(0)
        def url = "http://www.kegg.jp/pathway/" ;
        def listOfGenesIDs = result.get('ListOfGenesIDs').split(" ");

        for (int i = 0; i < listOfGenesIDs.size(); i++) {
            url += "+" + listOfGenesIDs[i]
        }

        //def rest = new RestBuilder();
        //def resp = rest.get(url);
        def httpBuilder = new AsyncHTTPBuilder([uri: url, poolSize: 10, contentType: HTML])

        def keggPageHTML = httpBuilder.request(GET,TEXT) { req ->
            uri.path = topPathway // overrides any path in the default URL

            response.success = { resp, reader ->
                assert resp.status == 200
            }

            // called only for a 404 (not found) status code:
            response.'404' = { resp ->
                println 'Not found'
            }
        }


        result.put("KeggHTML", keggPageHTML.get());
        //result.put("KeggHTML", resp.text)

        return result;
    }


    def eaeInterfaceSparkSubmit(String interfaceURL, Map paramMap ){
        def url = interfaceURL + "interfaceEAE/sparkSubmit/runSubmit" //"http://146.169.32.106:8081/interfaceEAE/sparkSubmit/runSubmit"
        def jsonBody = new JSONObject(paramMap).toString();
        def rest = new RestBuilder();

        def resp = rest.post(url){
            accept("application/text")
            contentType("application/json")
            body(jsonBody)
        }

        return resp.text

    }
}
