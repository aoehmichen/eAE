package eae.plugin
import com.mongodb.BasicDBObject
import grails.transaction.Transactional
import groovyx.net.http.AsyncHTTPBuilder
import org.apache.oozie.client.OozieClient
import org.json.JSONObject
import eae.plugin.RestServiceFactory

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST

@Transactional
class EaeService {

    def mongoCacheService

    /**
     *   Renders the default view
     */
    def getHpcScriptList() {
        def scriptList = ['Cross Validation', 'General Testing', 'Pathway Enrichment']
        return scriptList
    }

    //TODO: Depreciated. To be removed
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

    def customPreProcessing(params, workflow, MONGO_URL,MONGO_PORT,database,username){
        switch (workflow){
            case "CrossValidation":
                return cvPreprocessing(params, MONGO_URL,MONGO_PORT,database,username);
            default:
                throw new Exception("The workflow in customPreProcessing doesn't exist.")
        }
    }

    /**
     * Sets all required parameters for the cross validation pipeline.
     * @param params
     * @param MONGO_URL
     * @param MONGO_PORT
     * @param database
     * @param username
     * @return
     *
     * NB: Not the most elegant solution. TO BE IMPROVED
     */
    private def cvPreprocessing(params, MONGO_URL,MONGO_PORT,database,username){
        def workflowParameters = [:];
        String mongoDocumentIDPE = "abcd0000" ;// fake mongoId
        String doEnrichement = "false";
        String algorithmToUse = "SVM"
        String kfold= "0.2";
        String resampling = "1";
        String numberOfFeaturesToRemove = "0.4"

        if( ((String)params.doEnrichment).toBoolean()){
            def query = new BasicDBObject("StudyName" , "PathwayEnrichment")
            query.append("DataType","None")
            query.append("CustomField","")
            query.append("WorkflowSpecificParameters","Bonferroni")
            mongoDocumentIDPE = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, database, "PathwayEnrichment", "NoSQL", username, query )
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

    /**
     * This custom post processing is the only way to retrieve in real time the required data from KEGG
     * @param result
     * @return
     */
    private def pePostProcessing(result) {
        def topPathway = result.get('TopPathways').get(0).get(0)
        def url = "http://www.kegg.jp/pathway/" ;
        def listOfGenesIDs = result.get('ListOfGenesIDs').split(" ");

        def httpBuilder = new AsyncHTTPBuilder([uri: url, poolSize: 10, contentType: HTML])

        def keggPageHTML = httpBuilder.request(GET,TEXT) { req ->
            def finalUri = topPathway;
            for (int i = 0; i < listOfGenesIDs.size(); i++) {
                finalUri += "+" + listOfGenesIDs[i]
            }
            uri.path = finalUri // overrides any path in the default URL

            response.success = { resp, reader ->
                assert resp.status == 200
                reader.text
            }

            // called only for a 404 (not found) status code:
            response.'404' = { resp ->
                println 'Not found'
            }
        }

        result.put("KeggHTML", keggPageHTML.get());

        return result;
    }

    /**
     * This methods handles the remote submission to the eAEIterface which in return will trigger the Spark Job
     * I had to define all the queries manuall as the default rest plugin overrides some marshallers which causes massive
     * problem with tranSMART.
     * @param interfaceURL
     * @param paramMap
     * @return {str} : status of the submission
     */
    def eaeInterfaceSparkSubmit(String interfaceURL, Map paramMap ){
        //"https://146.169.15.140:8081/interfaceEAE/transmart/runSubmit"
        def httpBuilder = RestServiceFactory.initializeHttpBuilder(interfaceURL)
        def jsonBody = new JSONObject(paramMap).toString();
        def sparkSubmitStatus = httpBuilder.request(POST,TEXT) { req ->
            uri.path = "interfaceEAE/transmart/runSubmit" // overrides any path in the default URL
            body = jsonBody
            response.success = { resp, reader ->
                assert resp.status == 200
                reader.text
            }

            // called only for a 404 (not found) status code:
            response.'404' = { resp ->
                println '404 - Not found'
            }

            response.failure = { resp, text ->
                println("eAE - Error (${resp.status}) :: $text")
            }
        }

        return sparkSubmitStatus.get()
    }
}
