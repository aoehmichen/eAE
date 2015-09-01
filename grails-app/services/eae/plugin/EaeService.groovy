package eae.plugin

class EaeService {

    /**
     *   Renders the default view
     */
    def getHpcScriptList() {
        def scriptList = ['Cross Validation', 'GWAS - LP', 'Pathway Enrichment', 'General Testing']
        return scriptList
    }
    /**
     *   Gets the directory where all the eae scripts are located
     *
     *   @return {str}: path to the script folder
     */
    def getEAEScriptDir() {
        println(org.codehaus.groovy.grails.plugins.GrailsPluginUtils.getPluginDirForName('smart-r'))
        return org.codehaus.groovy.grails.plugins.GrailsPluginUtils.getPluginDirForName('smart-r').getFile().absolutePath + '/web-app/Scripts/'
        //return '/var/lib/tomcat7/webapps/transmart/plugins/smart-r-0.1/Scripts/eae/'
}

    def sparkSubmit(def sparkParameters, String scriptDir){
        def script = getEAEScriptDir() +'executeSparkJob.sh'
        def exitVal = [script, sparkParameters].execute().exitValue()
        return exitVal
    }

}
