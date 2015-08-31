package eae.plugin

import com.recomdata.transmart.domain.i2b2.AsyncJob
import org.apache.commons.lang.StringUtils
import org.json.JSONArray
import org.json.JSONObject

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

    def  SendToHDFS (def genesList, String scriptDir, String sparkURL ) {
        def script = scriptDir +'transferToHDFS.sh'
        def fileToTransfer = 'geneList.txt'

        File f =new File(genesList)
        if(f.exists()){
            f.delete()
        }
        f.createNewFile()
        String fp = f.getPath()

        def exitVal =[script, fp, fileToTransfer, sparkURL].execute().exitValue()

        // We cleanup
        f.delete()

        return exitVal

//              This code would work but requires the installation of the hadoop stack on the host with all the utils to work....
//                FileSystem hdfs =FileSystem.get(new URI("hdfs://146.169.32.196:8020"), new Configuration())
//                hdfs.copyFromLocalFile(fp,new Path('/home/hdfs/'))
    }

    /**
     * Method that will get the list of jobs to show in the eae jobs table
     */
    def getjobs(String userName, jobType = null) {
        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

        def jobResults = null
        def c = AsyncJob.createCriteria()
        if (StringUtils.isNotEmpty(jobType)) {
            jobResults = c {
                like("jobName", "${userName}%")
                eq("jobType", "${jobType}")
                ge("lastRunOn", new Date()-7)
                order("lastRunOn", "desc")
            }
        } else {
            jobResults = c {
                like("jobName", "${userName}%")
                or {
                    ne("jobType", "DataExport")
                    isNull("jobType")
                }
                ge("lastRunOn", new Date()-7)
                order("lastRunOn", "desc")
            }
        }

        def m = [:]
        def d
        for (jobResult in jobResults)	{
            m = [:]
            m["name"] = jobResult.jobName
            m["status"] = jobResult.jobStatus
            m["runTime"] = jobResult.jobStatusTime
            m["startDate"] = jobResult.lastRunOn
            m["viewerURL"] = jobResult.viewerURL
            m["altViewerURL"] = jobResult.altViewerURL
            m["jobInputsJson"] = new JSONObject(jobResult.jobInputsJson ?: "{}")
            d = getLatest(StatusOfExport.findAllByJobName(jobResult.jobName));
            if(!d.equals(null) ) {
                m["lastExportName"] = d.lastExportName;
                m["lastExportTime"] = d.lastExportTime.toString();
                m["exportStatus"] = d.jobStatus;
            }else{
                m["lastExportName"] = "Never Exported";
                m["lastExportTime"] = " ";
                m["exportStatus"] = " ";
            }
            rows.put(m)
        }

        result.put("success", true)
        result.put("totalCount", jobResults.size())
        result.put("jobs", rows)

        return result
    }
}
