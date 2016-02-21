package eae.plugin
import com.mongodb.BasicDBObject

class MongoCacheController {

    def mongoCacheService
    def eaeService
    def springSecurityService

    /**
     * Method that will create the get the list of jobs to show in the etriks jobs tab
     */
    def retrieveCachedJobs = {
        def username = springSecurityService.getPrincipal().username;
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;

        if(params.workflow == null) {
            throw new RuntimeException("The params in MongoCacheController are Null")
        }
        def result = mongoCacheService.getjobsFromMongo(MONGO_URL, MONGO_PORT, "eae", username, params.workflow )

        render result
    }

    def retrieveSingleCachedJob = {
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;

        BasicDBObject query = mongoCacheQuery(params, params.workflowtype);
        def result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT, "eae", params.workflow, query);
        result = eaeService.customPostProcessing(result, params.workflow)

        render result;
    }

    def retieveDataFromMongoFS = {
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        def dataSelected = params.dataSelected;
        def result = mongoCacheService.retrieveDataFromMongo(MONGO_URL, MONGO_PORT, "eae", dataSelected);
        render result
    }

    private def mongoCacheQuery(def params, String workflowType){
        //String workflowSelected = params.workflow;
        BasicDBObject query = new BasicDBObject();
        switch (workflowType){
            case "NoSQL":
                query.append("StudyName", params.studyname);
                query.append("DataType", params.datatype);
                query.append("CustomField", params.customfield);
                query.append("WorkflowSpecificParameters",params.workflowspecificparameters)
                break;
            default :
                query.append("WorkflowData", params.WorkflowData);
                query.append("result_instance_id1", params.result_instance_id1);
                query.append("result_instance_id2", params.result_instance_id2);
                break;
        }
        return query
    }

}
