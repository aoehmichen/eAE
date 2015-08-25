package eae.plugin
import grails.transaction.Transactional
import groovy.json.JsonBuilder
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.commons.vfs2

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


    def sendToHDFS(){
        try {
//                Configuration conf = new Configuration();
//                conf.set ( "fs.defaultFS", "hdfs://146.169.32.196:8020/user/hdfs" );
//
//                FileSystem fs = FileSystem.get(conf);
//
//                fs.createNewFile ( new Path ( "/user/hdfs/test" ) );
//
//                FileStatus[] status = fs.listStatus(new Path("/user/hdfs"));
//                for (
//                int i = 0;
//                i < status.length; i++) {
//                    System.out.println(status[i].getPath());
//                }


                File f=new File("abc.txt")//Takes the default path, else, you can specify the required path
                if(f.exists())
                {
                    f.delete()
                }
                f.createNewFile()

                FileObject destn=VFS.getManager().resolveFile(f.getAbsolutePath())
                FileSystem hdfs =FileSystem.get(new URI("hdfs://146.169.32.196:8020"), new Configuration())
                Path newFilePath=new Path(f)
                FSDataOutputStream out = hdfs.create(outFile)

                return true
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
