package eae.plugin
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoDatabase
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
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
        doc.append("Status", "Started");
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
            }else if(cacheItem.get("Status") == "Started" ){
                return "started"
            }else{
                return "Completed"
            }
        }
    }

    def buildMongoCacheQuery(params,parameterMap){
        def conceptBoxes = new JsonSlurper().parseText(params.conceptBoxes)
        String workflowData = conceptBoxes.concepts[0][0];
        BasicDBObject query = new BasicDBObject();
        query.append('patientids_cohort1', parameterMap['patientids_cohort1']);
        query.append('patientids_cohort2', parameterMap['patientids_cohort2']);
        query.append('WorkflowData', workflowData);
        query.append("DocumentType", "Original");
        return query
    }

    def buildMongoCacheQueryNoSQL(params){
        BasicDBObject query = new BasicDBObject();
        query.append('StudyName', params.studySelected);
        query.append('DataType', params.dataSelected);
        query.append('CustomField', params.customField.trim().split(",").sort(Collections.reverseOrder()).join(' ').trim());
        query.append('WorkflowSpecificParameters',params.workflowSpecificParameters);
        query.append("DocumentType", "Original");
        return query
    }
    /**
     * Method that will get the list of jobs to show in the eae jobs table
     */
    def getJobsFromMongo(String mongoURL, String mongoPort, String dbName, String userName, String workflowSelected) {

        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL,mongoPort);
        MongoDatabase  db = mongoClient.getDatabase( dbName );
        MongoCollection coll = db.getCollection(workflowSelected);

        BasicDBObject query = new BasicDBObject("User", userName);
        def cursor = coll.find(query).iterator();
        def rows;

        rows = retrieveRows(cursor);

        JSONObject res =  new JSONObject();
        res.put("success", true)
        res.put("totalCount", rows[1])
        res.put("jobs", rows[0])

        mongoClient.close();

        return res
    }

    def retrieveDataFromMongoFS(String mongoURL, String mongoPort, String dbName, String fileName){
        String extension = fileName.split('\\.')[1]
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(mongoURL, mongoPort);
        MongoDatabase db = mongoClient.getDatabase(dbName);
        GridFSBucket gridFSBucket = GridFSBuckets.create(db);

        OutputStream outputstream = new ByteArrayOutputStream();
        gridFSBucket.downloadToStreamByName(fileName, outputstream);
        String imageBase64 = outputstream.toByteArray().encodeAsBase64().toString()

        outputstream.flush();
        outputstream.close()
        mongoClient.close();

        String imageSrc = "data:image/"+ extension + ";base64,"+imageBase64;

        return imageSrc
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
                doc.append(key.toString(), reshapeArray((JSONArray)value));
            }else{
                if (!key.equals("_id")) {
                    doc.append(key.toString(), value);
                }
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

        coll.insertOne(doc);

        mongoClient.close();

        return 0
    }

    def reshapeArray(JSONArray value){
        def arrayList = new ArrayList();
        if (value.length()> 1){
            // Here I assume that all arrays contain n-tuples
            def tupleLength=0;
            def type = value.get(0).getClass();
            if( type == Double.class || type == Float.class || type == Long.class ||
                    type == Integer.class || type == Short.class || type == Character.class ||
                    type == Byte.class || type == Boolean.class || type == String.class){
                tupleLength = 1
            }else{
                tupleLength = value.get(0).length();
            }
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
        doc.append("patientids_cohort1", query.get("patientids_cohort1"));
        doc.append("patientids_cohort2", query.get("patientids_cohort2"));
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
            String patientids_cohort1 =  obj.get("patientids_cohort1");
            String patientids_cohort2 =  obj.get("patientids_cohort2");
            String name = "HighDim Data: " +  highDimName + "<br /> cohort 1 : " + patientids_cohort1 + "<br /> cohort 2 : " + patientids_cohort2;
            result.put("status", obj.get("Status"));
            result.put("start", obj.get("StartTime"));
            result.put("name", name);
            rows.put(result);
            count+=1;
        }

        return [rows, count]
    }
}
