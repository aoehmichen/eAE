package eae.plugin

class MongoCacheController {

    def MongoCacheService

    def RetireveFromCache = {

        //final galaxyURL = grailsApplication.config.com.galaxy.blend4j.galaxyURL;
        //final tempFolderDirectory = grailsApplication.config.com.recomdata.plugins.tempFolderDirectory;

        final String mongoURL = "localhost";
        final String mongoPort = "27017";
        final String dbName ="smartR"
        def returnValue = MongoCacheService.retrieveValueFromCache(mongoURL,mongoPort,dbName)

        render "hello"
    }

}
