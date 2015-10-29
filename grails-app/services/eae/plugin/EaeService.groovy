package eae.plugin

import com.mongodb.BasicDBObject
import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import org.apache.oozie.client.OozieClient

@Transactional
class EaeService {

    def mongoCacheService

    /**
     *   Renders the default view
     */
    def getHpcScriptList() {
        def scriptList = ['Cross Validation', 'Pathway Enrichment'] //['Cross Validation', 'GWAS - LP', 'Pathway Enrichment', 'General Testing']
        return scriptList
    }

    def scheduleOOzieJob(OOzie_URL){
        // get a OozieClient for local Oozie
        OozieClient wc = new OozieClient(OOzie_URL);

        // create a workflow job configuration and set the workflow application path
        Properties conf = wc.createConfiguration();
        conf.setProperty(OozieClient.USER_NAME, "centos");
        conf.setProperty(OozieClient.APP_PATH, "hdfs://eti-spark-master.novalocal:8020/user/centos/examples/apps/map-reduce/workflow.xml");
        // setting workflow parameters
        conf.setProperty("jobTracker", "eti-spark-master.novalocal:8032"); // the port must match yarn.resourcemanager.address's
        //conf.setProperty("inputDir", "/usr/tucu/inputdir");
        conf.setProperty("outputDir", "map-reduce");

        // submit and start the workflow job
        String jobId = wc.run(conf);
        return jobId;
    }

    def sparkSubmit(String scriptDir, String SparkURL, String worflowFileName, String dataFileName, String workflowSpecificParameters, String mongoDocumentID){
        def script = scriptDir +'executeSparkJob.sh'

        def scriptFile = new File(script)
        if (scriptFile.exists()) {
            if (!scriptFile.canExecute()) {
                scriptFile.setExecutable(true)
            }
        }else {
            log.error('The Script file spark submit wasn\'t found')
        }
        def executeCommand = script + " " + SparkURL + " " + worflowFileName + " " + dataFileName + " " + workflowSpecificParameters + " " + mongoDocumentID
        println(executeCommand)
        executeCommand.execute().waitFor()
        return 0
    }

    def customPreProcessing(params, workflow, MONGO_URL,MONGO_PORT,database,username){
        switch (workflow){
            case "cv":
                return cvPreprocessing(params, MONGO_URL,MONGO_PORT,database,username);
            case "gt":
                return gtPreprocessing(params); //TODO
            default:
                throw new Exception("The workflow in customPreProcessing doesn't exist.")
        }
    }

    private def cvPreprocessing(params, MONGO_URL,MONGO_PORT,database,username){

        String PEParameters = "false abcd0000" // fake mongoId
        if( params.doEnrichment){
            String mongoDocumentIDPE = mongoCacheService.initJob(MONGO_URL, MONGO_PORT, database, "pe", username, new BasicDBObject("ListOfGenes" , ""))
            PEParameters = "true " + mongoDocumentIDPE
        }

        String workflowSpecificParameters = "SVM featuresList.txt 0.2 1 0.5 " + PEParameters // "SVM" + additionalFileName + "0.2 1 0.5"

        return workflowSpecificParameters;
    }


    def customPostProcessing(result, workflow) {

        switch (workflow){
            case "pe":
                return pePostProcessing(result);
            default:
                return result
        }
    }

    private def pePostProcessing(result){
        def topPathway = result.get('TopPathways').get(0).get(0)
        def url = "http://www.kegg.jp/pathway/" + topPathway;
        def listOfGenesIDs = result.get('ListOfGenesIDs').split(" ");

        for (int i = 0; i < listOfGenesIDs.size(); i++) {
            url += "+" + listOfGenesIDs[i]
        }

        def rest = new RestBuilder();
        def resp = rest.get(url);
        result.put("KeggHTML", resp.text);

        return result;
    }
}
