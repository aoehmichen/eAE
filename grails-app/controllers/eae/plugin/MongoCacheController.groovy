package eae.plugin
import com.mongodb.BasicDBObject

class MongoCacheController {

    def mongoCacheService
    def eaeService
    def springSecurityService

    /**
     *
     * @return
     */
    private def mongoParams(){
        final String MONGO_USER = grailsApplication.config.com.eae.mongoUser;
        final char[] MONGO_PASSWORD = grailsApplication.config.com.eae.mongoPassword;
        return [ MONGO_USER, MONGO_PASSWORD];
    }


    /**
     * Method that will create the get the list of jobs to show in the etriks jobs tab
     *
     * @return {json} : returns a json containing all the jobs for the specified user and workflow
     */
    def retrieveCachedJobs = {
        def username = springSecurityService.getPrincipal().username;
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final def (mongoUser, mongoPassword) = mongoParams();

        if(params.workflow == null) {
            throw new RuntimeException("The params in MongoCacheController are Null")
        }
        def result = mongoCacheService.getJobsFromMongo(MONGO_URL, mongoUser, "eae", mongoPassword, username, params.workflow )

        render result
    }

    /**
     * Method that retrieves a single record from the cache
     *
     * @return {json} : returns the mongo document
     */
    def retrieveSingleCachedJob = {
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final def (mongoUser, mongoPassword) = mongoParams();
        BasicDBObject query = mongoCacheQuery(params, params.WorkflowType);
        def result = mongoCacheService.retrieveValueFromCache(MONGO_URL, mongoUser, "eae", mongoPassword, params.Workflow, query);
        result = eaeService.customPostProcessing(result, params.Workflow)
        render result;
    }

    /**
     * Method that retrieves a file from Mongo. It is currently used to support image storage and display in tranSMART.
     * NB: this function supports DICOM images but it require some further dev of the front end to get the full dicom support in tm.
     *
     * @return {str} : a serialized image contained in a string.
     */
    def retieveDataFromMongoFS = {
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final def (mongoUser, mongoPassword) = mongoParams();
        def dataSelected = params.FileName;
        def file = mongoCacheService.retrieveDataFromMongoFS(MONGO_URL, mongoUser, "eae", mongoPassword, dataSelected);
        render file
    }

    /**
     * Create the basic query with the appropriate fields depending if it is a SQL or NoSQL pipeline.
     * @param params
     * @param workflowType
     * @return {json} : query fields use for mongo.
     */
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
