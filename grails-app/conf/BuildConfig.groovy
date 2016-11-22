grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {
    log "warn"
    legacyResolve false
    inherits('global') {}
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
        mavenRepo 'https://repo.thehyve.nl/content/repositories/public/'
    }
    dependencies {
        compile 'org.mongodb:mongo-java-driver:3.3.0'
        compile 'org.apache.httpcomponents:httpcore:4.3.3'
        compile 'org.apache.httpcomponents:httpclient:4.3.6'
        compile ('org.codehaus.groovy.modules.http-builder:http-builder:0.5.1') {
            excludes('httpcore', 'httpclient')
        }
//        runtime ('xerces:xercesImpl:2.8.1'){
//            excludes "xml-apis"
//        }
        runtime ('net.sourceforge.nekohtml:nekohtml:1.9.21')
    }
    plugins {
        runtime ':resources:1.2.1'
    }
}