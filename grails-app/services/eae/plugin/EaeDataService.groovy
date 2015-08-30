package eae.plugin

import grails.transaction.Transactional
import org.apache.hadoop.fs.Path

@Transactional
class EaeDataService {

    def jobDataMap = [:]

    def eaeService

//    def studiesResourceService
//    def conceptsResourceService
//    def clinicalDataResourceService
//    def highDimExportService
//    def exportMetadataService


//    def getAllLowDim(){
//
//        def data = [:]
//        def rIID1 = jobDataMap['result_instance_id1']
//        def rIID2 = jobDataMap['result_instance_id2']
//        def cohort1 = rIID1 ? i2b2HelperService.getSubjectsAsList(rIID1).collect { it.toLong() } : []
//        def cohort2 = rIID2 ? i2b2HelperService.getSubjectsAsList(rIID2).collect { it.toLong() } : []
//        def cohorts = [cohort1, cohort2]
//
//        def toto = new File(jobDataMap['lowDimFile']).write(new JsonBuilder(data).toPrettyString())
//    }
//
//
//    def getClinicalMetaDataForEAE(Long resultInstanceId1, Long resultInstanceId2) {
//        return exportMetadataService.getClinicalMetaData(resultInstanceId1,resultInstanceId2)
//
//    }

    def  SendToHDFS (def genesList, String sparkURL ) {
        def script = eaeService.getEAEScriptDir()+'transferToHDFS.sh'
        def fileToTransfer = 'geneList.txt'

        File f =new File(genesList)
        if(f.exists()){
            f.delete()
        }
        f.createNewFile()
        Path fp = new Path(f.getPath())

        [script, fp, fileToTransfer, sparkURL].execute()

        // We cleanup
        f.delete()

//              This code would work but requires the installation of the hadoop stack on the host with all the utils to work....
//                FileSystem hdfs =FileSystem.get(new URI("hdfs://146.169.32.196:8020"), new Configuration())
//                hdfs.copyFromLocalFile(fp,new Path('/home/hdfs/'))
    }
}
