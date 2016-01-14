package eae.plugin
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoDatabase
import grails.transaction.Transactional
import groovy.json.JsonSlurper
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

    def copyPresentInCache(String mongoURL, String mongoPort, String dbName, String collectionName, BasicDBObject query) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort)
        MongoDatabase db = mongoClient.getDatabase( dbName )
        MongoCollection<Document> coll = db.getCollection(collectionName)

        def cursor = coll.find(query).iterator();
        def copyExists = false;

        while(cursor.hasNext()) {
            copyExists= true ;
            return copyExists
        }
        mongoClient.close()

        return copyExists;
    }

    def initJob(String mongoURL, String mongoPort, String dbName, String workflowSelected, String typeOfWorkflow, String user, BasicDBObject query){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase db = mongoClient.getDatabase( dbName );
        MongoCollection<Document> coll = db.getCollection(workflowSelected);

        Document cacheRecord = new Document();
        Document doc = new Document();
        doc.append("Status", "started");
        doc.append("User", user);
        doc.append("StartTime", new Date());
        doc.append("EndTime", new Date());
        doc.append("DocumentType","Original");
        switch (typeOfWorkflow) {
            case "NoSQL":
                cacheRecord = initJobNoSQL(doc, query);
                break;
            default:
                cacheRecord = initJobDefault(doc, query);
                break;

        }

        coll.insertOne(cacheRecord)
        def jobId = doc.get( "_id" );

        return jobId;
    }

    def checkIfPresentInCache(String mongoURL, String mongoPort, String dbName, String collectionName, query ){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase db = mongoClient.getDatabase( dbName );

        def cursor = db.getCollection(collectionName).find(query).iterator();
        def recordsCount = 0;
        JSONObject cacheItem;

        while(cursor.hasNext()) {
            cacheItem = new JSONObject(cursor.next().toJson());
            recordsCount+=1;
        }
        mongoClient.close();
        if(recordsCount>1){
           throw new Exception("Invalid number of records in the mongoDB")
        }else{
            if (recordsCount == 0){
                return "NotCached"
            }else if(cacheItem.get("Status") == "started" ){
                return "started"
            }else{
                return "Completed"
            }
        }
    }

    def buildMongoCacheQuery(params){
        def conceptBoxes = new JsonSlurper().parseText(params.conceptBoxes)
        String workflowData = conceptBoxes.concepts[0][0];
        BasicDBObject query = new BasicDBObject();
        query.append('result_instance_id1', params.result_instance_id1);
        query.append('result_instance_id2', params.result_instance_id2);
        query.append('WorkflowData', workflowData);
        query.append("DocumentType", "Original");
        return query
    }

    def buildMongoCacheQueryNoSQL(params){
        BasicDBObject query = new BasicDBObject();
        query.append('StudyName', params.studySelected);
        query.append('DataType', params.dataSelected);
//        query.append('Workflow', params.workflow);
        query.append('CustomField', params.customField.trim().split(",").sort(Collections.reverseOrder()).join(' ').trim());
        query.append('WorkflowSpecificParameters',params.workflowSpecificParameters);
        query.append("DocumentType", "Original");
        return query
    }
    /**
     * Method that will get the list of jobs to show in the eae jobs table
     */
    def getjobsFromMongo(String mongoURL, String mongoPort, String dbName, String userName, String workflowSelected) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase  db = mongoClient.getDatabase( dbName );
        MongoCollection coll = db.getCollection(workflowSelected);

        BasicDBObject query = new BasicDBObject("User", userName);
        def cursor = coll.find(query).iterator();
        def rows;

//        switch (workflowSelected) {
//            case "NoSQL":
        rows = retrieveRows(cursor);
//                break;
//            default:
//                rows = retrieveRowsDefault(cursor);
//                break;
//
//        }

        JSONObject res =  new JSONObject();
        res.put("success", true)
        res.put("totalCount", rows[1])
        res.put("jobs", rows[0])

        mongoClient.close();

        return res
    }

    /************************************************************************************************
     *                                                                                              *
     *  Pathway Enrichement section                                                                    *
     *                                                                                              *
     ************************************************************************************************/

    def initJobNoSQL(Document doc, query){
        doc.append("StudyName", query.get("StudyName"));
        doc.append("DataType", query.get("DataType"));
        doc.append("CustomField", query.get("CustomField"));
        doc.append("WorkflowSpecificParameters", query.get("WorkflowSpecificParameters"));
        return doc;
    }

    def retrieveRows(MongoCursor cursor){
        def rows = new JSONArray();
        JSONObject result;
        def count = 0;
        while(cursor.hasNext()) {
            JSONObject jObject =  new JSONObject(cursor.next().toJson());
            Iterator<?> keys = jObject.keys();
            result = new JSONObject();
            while( keys.hasNext() ) {
                String key = (String)keys.next();
                result.put(key.toLowerCase(),  jObject.get(key));
            }
            rows.put(result);
            count+=1;
        }
        return [rows, count]
    }

    def duplicateCacheForUser(String mongoURL, String mongoPort, String database, String workflow, String username, JSONObject cacheRes){
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> coll = db.getCollection(workflow);

        Document doc = new Document();
        Iterator<?> keys = cacheRes.keys();
        def value;
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            value = cacheRes.get(key);
            if(value instanceof JSONArray){
                doc.append(key, reshapeArray((JSONArray)value))
            }else {
                doc.append(key, value);
            }
        }

        doc.remove("StartTime");
        doc.remove("EndTime");
        doc.remove("User");
        doc.remove("DocumentType");

        doc.append("StartTime", new Date());
        doc.append("EndTime", new Date());
        doc.append("User", username);
        doc.append("DocumentType", "Copy");

        coll.insertOne(doc)

        return 0
    }

    def reshapeArray(JSONArray value){
        def arrayList = new ArrayList();
        if (value.length()> 1){
            // Here I assume that all arrays contain n-tuples
            def tupleLength = value.get(0).length()
            for(int i=0; i < value.length(); i++){
                if(tupleLength == 1){
                    arrayList.add(i, value.get(i))
                }else {
                    def newArray = []
                    for (int j = 0; j < tupleLength; j++) {
                        newArray.add(j, value.get(i).get(j))
                    }
                    arrayList.add(i,newArray)
                }
            }
        }
        return arrayList;
    }

/************************************************************************************************
 *                                                                                              *
 *  Worflow Default section                                                                    *
 *                                                                                              *
 ************************************************************************************************/

    def initJobDefault(Document doc, BasicDBObject query){
        doc.append("WorkflowData", query.get("WorkflowData"));
        doc.append("result_instance_id1", query.get("result_instance_id1"));
        doc.append("result_instance_id2", query.get("result_instance_id2"));
        return doc;
    }

    def retrieveRowsDefault(MongoCursor cursor){
        def rows = new JSONArray();
        JSONObject result;
        def count = 0;
        while(cursor.hasNext()) {
            JSONObject obj =  new JSONObject(cursor.next().toJson());
            result = new JSONObject();
            String highDimName =  obj.get("WorkflowData");
            String result_instance_id1 =  obj.get("result_instance_id1");
            String result_instance_id2 =  obj.get("result_instance_id2");
            String name = "HighDim Data: " +  highDimName + "<br /> cohort 1 : " + result_instance_id1 + "<br /> cohort 2 : " + result_instance_id2;
            result.put("status", obj.get("Status"));
            result.put("start", obj.get("StartTime"));
            result.put("name", name);
            rows.put(result);
            count+=1;
        }

        return [rows, count]
    }
}
