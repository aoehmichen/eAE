package eae.plugin

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import grails.transaction.Transactional
import mongo.MongoCacheFactory
import org.bson.Document

@Transactional
class EaeNoSQLDataService {

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


    def getStudies(String mongoURL, String dbName, String workflowSelected){
        def url = mongoURL.split(':');
        MongoClient mongoClient = MongoCacheFactory.getMongoConnection(url[0], url[1]);
        MongoDatabase db = mongoClient.getDatabase( dbName );
        def toto = mongoClient.getDatabase(dbName).getProperties();
        return db.getCollectionNames();

    }

    def getDataTypeForStudy(String mongoURL, String dbName, String selectedStudy){
        MongoCollection<Document> coll = getMongoCollection(mongoURL, dbName, selectedStudy);
//        BasicDBObject query = new BasicDBObject("StudyName", selectedStudy);
        return coll.distinct("DataType");
    }

    def getListOfAvailableDataForStudy(){

        return
    }
}
