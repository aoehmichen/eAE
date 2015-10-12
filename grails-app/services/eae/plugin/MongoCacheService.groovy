package eae.plugin
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoDatabase
import grails.transaction.Transactional
import mongo.MongoCacheFactory
import org.bson.Document
import org.json.JSONArray
import org.json.JSONObject

@Transactional
class MongoCacheService {

    def retrieveValueFromCache(String mongoURL, String mongoPort, String dbName, String collectionName, BasicDBObject query) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort)
        MongoDatabase db = mongoClient.getDatabase( dbName )
        MongoCollection<Document> coll = db.getCollection(collectionName)

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

    def checkIfPresentInPECache(String mongoURL, String mongoPort, String paramValue){
        BasicDBObject query = new BasicDBObject("ListOfgenes", paramValue);
        return  checkIfPresentInCache(mongoURL,mongoPort,"eae","pe",query)
    }

    def checkIfPresentInCache(String mongoURL, String mongoPort, String dbName, String collectionName, BasicDBObject query ){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase db = mongoClient.getDatabase( dbName );

        def cursor = db.getCollection(collectionName).find(query).iterator();
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

        BasicDBObject query = new BasicDBObject("user", userName);
        def cursor = coll.find(query).iterator();
        def rows;

        switch (workflowSelected) {
            case "pe":
                rows = retrieveRowsForPE(cursor);
                break;
            case "gt":
                rows = retrieveRowsForGT(cursor);
                break;
            case "cv":
                rows = retrieveRowsForCV(cursor);
                break;
            case "lp":
                rows = retrieveRowsForLP(cursor);
                break;
        }

        JSONObject res =  new JSONObject();
        res.put("success", true)
        res.put("totalCount", rows[1])
        res.put("jobs", rows[0])

        mongoClient.close();

        return res
    }

    def retrieveRowsForPE(MongoCursor cursor){
        def rows = new JSONArray();
        JSONObject result;
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

        return [rows, count]
    }

    def duplicatePECacheForUser(String mongoURL, String mongoPort, String username, JSONObject cacheRes){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase db = mongoClient.getDatabase("eae");
        MongoCollection<Document> coll = db.getCollection("pe");

        def arrayList = new ArrayList();
        def topPath = (JSONArray)cacheRes.get("topPathways")
        for(int i =0; i < topPath.length();i++){
            arrayList.add(i,[topPath.get(i).get(0),topPath.get(i).get(1)])
       }

        Document doc = new Document();
        doc.append("topPathways", arrayList)
        doc.append("KeggTopPathway",cacheRes.get("KeggTopPathway") )
        doc.append("status", "Completed")
        doc.append("user", username)
        doc.append("ListOfgenes",cacheRes.get("ListOfgenes") )
        doc.append("Correction",cacheRes.get("Correction") )
        doc.append("StartTime", new Date())
        doc.append("EndTime", new Date())

        coll.insertOne(doc)

        return 0
    }
}
