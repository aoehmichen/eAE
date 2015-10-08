package eae.plugin

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import grails.transaction.Transactional
import mongo.MongoCacheFactory
import org.bson.Document
import org.json.JSONArray
import org.json.JSONObject

@Transactional
class MongoCacheService {

    def retrieveValueFromCache(String mongoURL, String mongoPort, String dbName, String paramValue) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort)
        MongoDatabase db = mongoClient.getDatabase( dbName )
        MongoCollection<Document> coll = db.getCollection("pe")

        BasicDBObject query = new BasicDBObject("ListOfgenes", paramValue)
        def result = new JSONObject(((Document)coll.find(query).first()).toJson())
        mongoClient.close()

        return result;
    }

    def initJob(String mongoURL, String mongoPort, String dbName, String workflow, String user, String geneList){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase db = mongoClient.getDatabase( dbName );
        MongoCollection<Document> coll = db.getCollection(workflow);

        Document doc = new Document();
        doc.append("topPathways", [])
        //doc.append("corrected_pValues", [])
        doc.append("KeggTopPathway", "")
        doc.append("status", "started")
        doc.append("user", user)
        doc.append("ListOfgenes", geneList)
        doc.append("Correction", "")
        doc.append("StartTime", new Date())
        doc.append("EndTime", new Date())

        coll.insertOne(doc)
        def jobId = doc.get( "_id" );

        return jobId;
    }

    def checkIfPresentInCache(String mongoURL, String mongoPort, String dbName, String paramValue){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase db = mongoClient.getDatabase( dbName );

        BasicDBObject query = new BasicDBObject("ListOfgenes", paramValue);
        def cursor = db.getCollection("pe").find(query).iterator();
        def recordsCount = 0;
        JSONObject cacheItem;

        while(cursor.hasNext()) {
            cacheItem =new JSONObject(cursor.next().toJson());
            recordsCount+=1;
        }
        mongoClient.close();
        if(recordsCount>1){
            throw new Exception("Invalid number of records in the mongoDB")
        }else{
            if (recordsCount == 0){
                return "NotCached"
            }else if(cacheItem.get("status") == "started" ){
                return "started"
            }else{
                return "Completed"
            }
        }
    }

    /**
     * Method that will get the list of jobs to show in the eae jobs table
     */
    def getjobsFromMongo(String mongoURL, String mongoPort, String dbName, String userName, String workflowSelected) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase  db = mongoClient.getDatabase( dbName );
        MongoCollection coll = db.getCollection(workflowSelected);

        JSONObject result;
        JSONArray rows = new JSONArray();
        BasicDBObject query = new BasicDBObject("user", userName);
        def cursor = coll.find(query).iterator();
        def count = 0;

        while(cursor.hasNext()) {
            JSONObject obj =  new JSONObject(cursor.next().toJson());
            result = new JSONObject();
            String name =  obj.get("ListOfgenes");
            result.put("status", obj.get("status"));
            result.put("start", obj.get("StartTime"));
            result.put("name", name);
            rows.put(result);
            count+=1;
        }

        JSONObject res =  new JSONObject();
        res.put("success", true)
        res.put("totalCount",count)
        res.put("jobs", rows)

        mongoClient.close();

        return res
    }
}
