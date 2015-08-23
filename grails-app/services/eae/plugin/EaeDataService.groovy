package eae.plugin

import grails.transaction.Transactional
import groovy.json.JsonBuilder

@Transactional
class EaeDataService {

    def jobDataMap = [:]

    def studiesResourceService
    def conceptsResourceService
    def clinicalDataResourceService
    def highDimExportService
    def exportMetadataService


    def getAllLowDim(){

        def data = [:]
        def rIID1 = jobDataMap['result_instance_id1']
        def rIID2 = jobDataMap['result_instance_id2']
        def cohort1 = rIID1 ? i2b2HelperService.getSubjectsAsList(rIID1).collect { it.toLong() } : []
        def cohort2 = rIID2 ? i2b2HelperService.getSubjectsAsList(rIID2).collect { it.toLong() } : []
        def cohorts = [cohort1, cohort2]

        def toto = new File(jobDataMap['lowDimFile']).write(new JsonBuilder(data).toPrettyString())
    }


    def getClinicalMetaDataForEAE(Long resultInstanceId1, Long resultInstanceId2) {
        return exportMetadataService.getClinicalMetaData(resultInstanceId1,resultInstanceId2)

    }
}
