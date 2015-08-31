package eae.plugin

class MongoCacheController {

    def mongoCacheService

    def RetireveFromCache = {

        final String mongoURL = "localhost";
        final String mongoPort = "27017";
        final String dbName ="smartR"
        def returnValue = mongoCacheService.retrieveValueFromCache(mongoURL,mongoPort,dbName)

        render "hello"
    }

}
