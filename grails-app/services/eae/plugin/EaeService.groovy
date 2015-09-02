package eae.plugin

class EaeService {

    /**
     *   Renders the default view
     */
    def getHpcScriptList() {
        def scriptList = ['Cross Validation', 'GWAS - LP', 'Pathway Enrichment', 'General Testing']
        return scriptList
    }

    def sparkSubmit(String scriptDir, String sparkParameters){
        def script = scriptDir +'executeSparkJob.sh'
        [script, sparkParameters].execute().waitFor()
        return 0
    }

}
