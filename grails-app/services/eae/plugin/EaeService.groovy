package eae.plugin

import grails.converters.JSON
import groovy.json.JsonBuilder
import org.springframework.beans.factory.annotation.Autowired


class EaeService {




    /**
     *   Renders the default view
     */
    def getHpcScriptList() {
        def scriptList = ['Cross Validation', 'GWAS - LP', 'Pathway Enrichment', 'General Testing']
        return scriptList
    }




}
