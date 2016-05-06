package mongo

import com.mongodb.MongoClient

public class MongoCacheFactory {

    /**
    *
    * @param IPAdress
    * @param port
    * @return @return {MonClient} : client to be used for running the queries
    */
    static def getMongoConnection(String IPAdress, String port){
        int portToUse = Integer.parseInt(port)
        return new MongoClient(IPAdress,portToUse);
    }

}
