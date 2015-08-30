package eae.plugin
import org.apache.commons.io.FilenameUtils

class EaeController {

    def smartRService
    def eaeDataService
    def eaeService
    def mongoCacheService


    final String SPARK_URL = grailsApplication.config.com.eae.sparkURL;
    final String MONGO_URL = grailsApplication.config.com.eae.mongoURL;
    final String MONGO_PORT = grailsApplication.config.com.eae.mongoPort;
    final String scriptDir = eaeService.EAEScriptDir;


    /**
     *   Go to SmartR
     */
    def goToSmartR = {
        render template: '/smartR/index', model:[ scriptList: smartRService.scriptList] }

    /**
     *   Renders the input form for initial script parameters
     */
    def renderInputs = {
        if (! params.script) {
            render 'Please select a script to execute.'
        } else {
            render template: '/eae/' + 'in' + FilenameUtils.getBaseName(params.script).replaceAll("\\s","")
        }
    }

//    def getClinicalMetaDataforEAE = {
//        List<Long> resultInstanceIds = parseResultInstanceIds()
//
//        render EaeDataService.getClinicalMetaDataForEAE(
//                resultInstanceIds[0],
//                resultInstanceIds[1]) as JSON
//    }
//
//
//    private List<Long> parseResultInstanceIds () {
//        List<Long> result = []
//        int subsetNumber = 1
//        while (params.containsKey('result_instance_id' + subsetNumber)) {
//            result << params.long('result_instance_id' + subsetNumber)
//            subsetNumber += 1
//        }
//        result
//    }

    def runPEForSelectedGenes = {
        println(params)

        String saneGenesList = ((String)params.genesList).trim().split(",").sort().join("\\t")
        println(saneGenesList)
        println(SPARK_URL)
        // We check if this query has already been made before
        Boolean cached = mongoCacheService.checkIfPresentInCache(MONGO_URL, MONGO_PORT, "eae", saneGenesList)

        if(!cached) {
            def sparkParameters = "pe.py pe_genes.txt Bonferroni"
            eaeDataService.SendToHDFS(saneGenesList, scriptDir, SPARK_URL)
            println("sent to HDFS")
            eaeService.sparkSubmit(sparkParameters)
            println("spark job submitted")
            render params.genesList.toString()
        }else{
            render params.genesList.toString()
        }
    }
}
