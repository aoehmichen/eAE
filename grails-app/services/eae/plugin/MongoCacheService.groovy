package eae.plugin

import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.MongoClient
import grails.transaction.Transactional
import mongo.MongoCacheFactory

@Transactional
class MongoCacheService {

    def retrieveValueFromCache(String mongoURL, String mongoPort, String dbName) {

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
}
