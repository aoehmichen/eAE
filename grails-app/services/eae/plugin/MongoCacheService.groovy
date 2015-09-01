package eae.plugin

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.MongoClient
import grails.transaction.Transactional
import mongo.MongoCacheFactory
import org.json.JSONArray
import org.json.JSONObject

@Transactional
class MongoCacheService {

    def retrieveValueFromCache(String mongoURL, String mongoPort, String dbName, String paramValue) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        DB db = mongoClient.getDB( dbName );
        BasicDBObject doc = new BasicDBObject()
        doc.put("name", "MongoDB")
        doc.put("type", "database")
        doc.put("count", 1)

        def creationResult = db.createCollection("testCollection",doc);
        System.err.println(creationResult);
        DBCollection coll = db.getCollection("testCollection")

        for(i in 1..100) {
            coll.insert(new BasicDBObject().append("i", i))
        }

        println coll.getCount()

        DBCursor cursor = coll.find()
        while(cursor.hasNext()) {
            println cursor.next()
        }

        mongoClient.close();

        return 0;
    }

    def initJob(String mongoURL, String mongoPort, String dbName, String user){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        DB db = mongoClient.getDB( dbName );
        DBCollection coll = db.getCollection("jobs");

        BasicDBObject doc = new BasicDBObject();
        doc.put("topPathways", [])
        doc.put("corrected_pValues", [])
        doc.put("KeggTopPathway", "")
        doc.put("status", "started")
        doc.put("user", user)
        doc.put("ListOfgenes", "")
        doc.put("Correction", "")

        coll.insert(doc)
        def jobId = doc.get( "_id" );

        return jobId;
    }

    def checkIfPresentInCache(String mongoURL, String mongoPort, String dbName, String paramValue){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        DB db = mongoClient.getDB( dbName );
        DBCollection coll = db.getCollection("pe")

        def recordsCount = coll.find({ListOfgenes: paramValue}).countParallel()
        if(recordsCount>1){
            throw new Exception("Invalid number of records in the mongoDB")
        }else{
            return recordsCount == 1;
        }
    }

    /**
     * Method that will get the list of jobs to show in the eae jobs table
     */
    def getjobs(String mongoURL, String mongoPort, String dbName, String userName, String workflowSelected) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        DB db = mongoClient.getDB( dbName );
        DBCollection coll = db.getCollection("jobs");

        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

        def cursor = coll.find($and: [ { user: userName} , {workflow : workflowSelected}]);

        while(cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            result = new JSONObject();
            BasicDBList name = (BasicDBList) obj.get("name");
            result.put("status", obj.getString("status"));
            result.put("startDate", obj.getString("startDate"));
            result.put("name", name);
            rows.put(result);
        }

        println(rows)
        result.put("success", true)
        result.put("totalCount", cursor.countParallel())
        result.put("jobs", rows)

        return result
    }
}
