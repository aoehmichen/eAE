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
                break;
            case "cv":
                query.append("HighDimData", params.high_dim_data);
                query.append("result_instance_id1", params.result_instance_id1);
                query.append("result_instance_id2", params.result_instance_id2);
                break;
            case "lp":
                break;
        }
        return query
    }

}
