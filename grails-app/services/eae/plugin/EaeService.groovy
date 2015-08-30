package eae.plugin

import org.codehaus.groovy.grails.plugins.GrailsPluginUtils


class EaeService {

    /**
     *   Renders the default view
     */
    def getHpcScriptList() {
        def scriptList = ['Cross Validation', 'GWAS - LP', 'Pathway Enrichment', 'General Testing']
        return scriptList
    }

    /**
     *   Gets the directory where all the R scripts are located
     *
     *   @return {str}: path to the script folder
     */
    def getEAEScriptDir() {
        return GrailsPluginUtils.getPluginDirForName('smart-r').getFile().absolutePath + '/web-app/Scripts/eae/'
    }

    def sparkSubmit(def sparkParameters){
        def script = getEAEScriptDir()+'executeSparkJob.sh'
        [script, sparkParameters].execute()
    }

}
