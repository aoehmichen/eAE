package smartR.plugin

import groovy.json.JsonBuilder
import grails.util.Holders
import grails.util.Environment
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils


class SmartRService {

    def DEBUG = Environment.current == Environment.DEVELOPMENT
    def DEBUG_TMP_DIR = '/tmp/'

    def grailsApplication = Holders.grailsApplication
    def springSecurityService
    def i2b2HelperService
    def dataQueryService

    def getScriptList() {
        def dir = getWebAppFolder() + 'Scripts/smartR/'
        def scriptList = []
        new File(dir).eachFile {
            def name = it.getName()
            if (name[0] != '.') scriptList << name
        }
        scriptList
    }

    def getWebAppFolder() {
        if (Environment.current == Environment.DEVELOPMENT) {
            return GrailsPluginUtils
                    .getPluginDirForName('smart-r')
                    .getFile()
                    .absolutePath + '/web-app/'
        } else {
            return grailsApplication
                    .mainContext
                    .servletContext
                    .getRealPath('/plugins/') + '/smart-r-0.5/'
        }
    }

    def queryData(rIID1, rIID2, conceptBoxes) {
        def data_cohort1 = [:]
        def data_cohort2 = [:]

        def patientIDs_cohort1 = rIID1 && rIID1 != 'null' ? i2b2HelperService.getSubjectsAsList(rIID1).collect { it.toLong() } : []
        def patientIDs_cohort2 = rIID2 && rIID2 != 'null' ? i2b2HelperService.getSubjectsAsList(rIID2).collect { it.toLong() } : []

        conceptBoxes.each { conceptBox ->
            conceptBox.cohorts.each { cohort ->
                def rIID
                def data
                def patientIDs

                if (cohort == 1) {
                    rIID = rIID1
                    patientIDs = patientIDs_cohort1
                    data = data_cohort1
                } else {
                    rIID = rIID2
                    patientIDs = patientIDs_cohort2
                    data = data_cohort2
                }

                if (! rIID || ! patientIDs) return

                if (conceptBox.concepts.size() == 0) {
                    data[conceptBox.name] = [:]
                } else if (conceptBox.type == 'valueicon' || conceptBox.type == 'alphaicon') {
                    data[conceptBox.name] = dataQueryService.getAllData(conceptBox.concepts, patientIDs)
                } else if (conceptBox.type == 'hleaficon') {
                    def rawData = dataQueryService.exportHighDimData(
                            conceptBox.concepts,
                            patientIDs,
                            rIID as Long)
                    data[conceptBox.name] = rawData
                } else {
                    throw new IllegalArgumentException()
                }
            }
        }

        def data = [:]
        data.cohort1 = new JsonBuilder(data_cohort1).toString()
        data.cohort2 = new JsonBuilder(data_cohort2).toString()

        if (DEBUG) {
            new File(DEBUG_TMP_DIR + 'data1.json').write(data.cohort1)
            new File(DEBUG_TMP_DIR + 'data2.json').write(data.cohort2)
        }

        data
    }
}
