package eae.plugin

class EaeService {

    /**
     *   Renders the default view
     */
    def getHpcScriptList() {
        def scriptList = ['Cross Validation', 'GWAS - LP', 'Pathway Enrichment', 'General Testing']
        return scriptList
    }

    def sparkSubmit(def sparkParameters, String scriptDir){
        def script = scriptDir +'executeSparkJob.sh'
        def exitVal = [script, sparkParameters].execute().exitValue()
        return exitVal
    }

}
