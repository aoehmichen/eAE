package eae.plugin

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import grails.transaction.Transactional
import mongo.MongoCacheFactory
import org.bson.Document
import org.json.JSONObject

@Transactional
class EaeNoSQLDataService {

    /**
     * The method retrieves the specified mongo collection
     * @param mongoURL
     * @param dbName
     * @param collectionName
     * @return {MongoCollection}
     */
    def getMongoCollection(String mongoURL, String dbName, String collectionName){
        def url = mongoURL.split(':');
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(url[0], url[1]);
        MongoDatabase db = mongoClient.getDatabase( dbName );
        MongoCollection<Document> coll = db.getCollection(collectionName);
        return coll;
    }

    def queryData(String mongoURL, String dbName, String collectionName, params) {
        MongoCollection<Document> coll = getMongoCollection(mongoURL, dbName, collectionName);

    }

    /**
     * The method retrieves all availables studies stored in Mongo
     * @param mongoURL
     * @param dbName
     * @param workflowSelected
     * @return
     */
    def getStudies(String mongoURL, String dbName, String workflowSelected){
        MongoCollection<Document> coll = getMongoCollection(mongoURL, dbName, "metadata");
        def listOfStudies = coll.distinct("StudyName",String.class).toList();
        return listOfStudies;
    }

    /**
     * The method retrieves all data types
     * @param mongoURL
     * @param dbName
     * @return
     */
    def getMongoData(String mongoURL, String dbName){
        MongoCollection<Document> coll = getMongoCollection(mongoURL, dbName, "custom_data");
        def dataTypes = coll.distinct("DataType",String.class).toList();
        return dataTypes
    }

    /**
     * the method retrieves all data types contained in a specific study
     * @param mongoURL
     * @param dbName
     * @param selectedStudy
     * @return
     */
    def getDataTypesForStudy(String mongoURL, String dbName, String selectedStudy){
        MongoCollection<Document> coll = getMongoCollection(mongoURL, dbName, "metadata");
        BasicDBObject query = new BasicDBObject();
        query.append('StudyName', selectedStudy);

        def dataTypesList = coll.distinct("DataType",query,String.class).toList();
        JSONObject dataTypes =  new JSONObject();
        dataTypes.put("dataList", dataTypesList)
        return dataTypes;
    }

}
