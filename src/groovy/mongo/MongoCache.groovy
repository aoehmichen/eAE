package mongo

import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress

public class MongoCacheFactory {

    /**
     *
     * @param IPAddress
     * @param port
     * @param user
     * @param database
     * @param password
     * @return {MonClient} : client to be used for running the queries
     */
    static def getMongoConnection(String IPAddress, String port, String user, String database, char[] password){
        int portToUse = Integer.parseInt(port)
        ServerAddress address = new ServerAddress(IPAddress, portToUse)
        MongoCredential credential = MongoCredential.createCredential(user, database, password);
        return new MongoClient(address, Arrays.asList(credential));
    }

}
