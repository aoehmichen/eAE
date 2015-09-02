package eae.plugin
//import org.apache.hadoop.fs.Path

class EaeDataService {

    def jobDataMap = [:]

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

    def  SendToHDFS (String genesList, String scriptDir, String sparkURL ) {
        def script = scriptDir +'transferToHDFS.sh'
        def fileToTransfer = 'geneList.txt'

        File f =new File("/tmp/eae/","geneList.txt")
        if(f.exists()){
            f.delete()
        }
        f.withWriter('utf-8') { writer ->
            writer.writeLine genesList
        } // or << genesList
        boolean created = f.createNewFile()
        String fp = f.getAbsolutePath() + "geneList.txt"
        println(f.exists())
        println(created)
        println(fp)
        println(script)
        println(fileToTransfer)
        def executeCommand = script + " " + fp + " "  + fileToTransfer + " " + sparkURL
        println(executeCommand)
        executeCommand.execute().waitFor()

        // We cleanup
        f.delete()

        return 0

//              This code would work but requires the installation of the hadoop stack on the host with all the utils to work....
//                FileSystem hdfs =FileSystem.get(new URI("hdfs://146.169.32.196:8020"), new Configuration())
//                hdfs.copyFromLocalFile(fp,new Path('/home/hdfs/'))
    }


}
