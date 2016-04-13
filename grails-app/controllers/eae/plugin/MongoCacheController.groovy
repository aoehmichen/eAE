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
        def result = mongoCacheService.getJobsFromMongo(MONGO_URL, MONGO_PORT, "eae", username, params.workflow )

        render result
    }

    def retrieveSingleCachedJob = {
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        BasicDBObject query = mongoCacheQuery(params, params.WorkflowType);
        def result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT, "eae", params.Workflow, query);
        result = eaeService.customPostProcessing(result, params.Workflow)
        render result;
    }

    def retieveDataFromMongoFS = {
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
        def dataSelected = params.FileName;
        def file = mongoCacheService.retrieveDataFromMongoFS(MONGO_URL, MONGO_PORT, "eae", dataSelected);
        render file
    }

    private def mongoCacheQuery(def params, String workflowType){
        BasicDBObject query = new BasicDBObject();
        switch (workflowType){
            case "NoSQL":
                query.append("StudyName", params.StudyName);
                query.append("DataType", params.DataType);
                query.append("CustomField", params.CustomField);
                query.append("WorkflowSpecificParameters",params.WorkflowSpecificParameters)
                break;
            default :
                query.append("WorkflowData", params.WorkflowData);
                query.append("patientids_cohort1", params.patientids_cohort1);
                query.append("patientids_cohort2", params.patientids_cohort2);
                break;
        }
        return query
    }

}
