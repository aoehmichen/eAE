package eae.plugin

import com.mongodb.BasicDBObject

class MongoCacheController {

    def mongoCacheService
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
        //else {
//
//            switch (params.script) {
//                case "Pathway Enrichment":
//                    workflow = "pe";
//                    break;
//                case "General Testing":
//                    workflow = "gt";
//                    break;
//                case "Cross Validation":
//                    workflow = "cv";
//                    break;
//                case "Label Propagation":
//                    workflow = "lp";
//                    break;
//                default:
//                    throw new Exception("The workflow doesn't exist.")
//            }
//        }
        def result = mongoCacheService.getjobsFromMongo(MONGO_URL, MONGO_PORT, "eae", username, params.workflow )

        render result
    }

    def retrieveSingleCachedJob = {
        final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
        final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;

        BasicDBObject query = mongoCacheQuery(params)
        def result = mongoCacheService.retrieveValueFromCache(MONGO_URL, MONGO_PORT,"eae", params.workflow, query)

        render result;
    }

    private def mongoCacheQuery(def params){
        String workflowSelected = params.workflow;
        BasicDBObject query = new BasicDBObject();
        switch (workflowSelected){
            case "pe":
                def saneGenesList = params.cacheQuery;
                query = new BasicDBObject("ListOfgenes", saneGenesList);
                break;
            case "gt":
                query = new BasicDBObject();
                break;
            case "cv":
                query = new BasicDBObject();
                break;
            case "lp":
                query = new BasicDBObject();
                break;
        }
        return query
    }

}
