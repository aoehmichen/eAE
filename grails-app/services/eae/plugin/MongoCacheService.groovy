package eae.plugin
import com.mongodb.*
import grails.transaction.Transactional
import mongo.MongoCacheFactory
import org.apache.poi.hssf.record.formula.functions.Now
import org.json.JSONArray
import org.json.JSONObject

@Transactional
class MongoCacheService {

    def retrieveValueFromCache(String mongoURL, String mongoPort, String dbName, String paramValue) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort)
        DB db = mongoClient.getDB( dbName )
        DBCollection coll = db.getCollection("pe")

        BasicDBObject query = new BasicDBObject("ListOfgenes", paramValue)
        def result = coll.find(query)
        mongoClient.close()

        return result;
    }

    def initJob(String mongoURL, String mongoPort, String dbName, String workflow, String user, String geneList){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        DB db = mongoClient.getDB( dbName );
        DBCollection coll = db.getCollection(workflow);

        BasicDBObject doc = new BasicDBObject();
        doc.put("topPathways", [])
        doc.put("corrected_pValues", [])
        doc.put("KeggTopPathway", "")
        doc.put("status", "started")
        doc.put("user", user)
        doc.put("ListOfgenes", geneList)
        doc.put("Correction", "")
        doc.put("StartTime", new Date())
        doc.put("EndTime", new Date())

        coll.insert(doc)
        def jobId = doc.get( "_id" );

        return jobId;
    }

    def checkIfPresentInCache(String mongoURL, String mongoPort, String dbName, String paramValue){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        DB db = mongoClient.getDB( dbName );

        BasicDBObject query = new BasicDBObject("ListOfgenes", paramValue);
        def coll = db.getCollection("pe").find(query)

        def recordsCount = coll.count()
        mongoClient.close();
        if(recordsCount>1){
            throw new Exception("Invalid number of records in the mongoDB")
        }else{
            if (recordsCount == 0){
                return "NotCached"
            }else if(coll.get("status") == "started" ){
                return "started"
            }else{
                return "Completed"
            }
        }
    }

    /**
     * Method that will get the list of jobs to show in the eae jobs table
     */
    def getjobs(String mongoURL, String mongoPort, String dbName, String userName, String workflowSelected) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        DB db = mongoClient.getDB( dbName );
        DBCollection coll = db.getCollection(workflowSelected);

        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

        def cursor = coll.find({ user: userName});

        while(cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            result = new JSONObject();
            BasicDBList name = (BasicDBList) obj.get("ListOfgenes");
            result.put("status", obj.getString("status"));
            result.put("startDate", obj.getString("startTime"));
            result.put("name", name);
            rows.put(result);
        }

        println(rows)
        result.put("success", true)
        result.put("totalCount", cursor.countParallel())
        result.put("jobs", rows)

        mongoClient.close();

        return result
    }
}
