class eaeGrailsPlugin {
    // the plugin version
    def version = "0.9"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "eAE" // Headline display name of the plugin
    def author = "Axel Oehmichen"
    def authorEmail = "ao1011@imperial.ac.uk"
    def description =
            '''
            Brief summary/description of the plugin.
            '''

    // URL to the plugin's documentation
    def documentation = ""

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]


    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        dataQueryService(eae.plugin.DataQueryService) {
            studiesResourceService = ref('studiesResourceService')
            conceptsResourceService = ref('conceptsResourceService')
            clinicalDataResourceService = ref('clinicalDataResourceService')
            dataSource = ref('dataSource')
            i2b2HelperService = ref('i2b2HelperService')
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        // TODO Implement post initialization spring config (optional)
        // Set the scripts to executable
        def eaeFileSystemName = ctx.getBean('pluginManager').allPlugins.sort({ it.name.toUpperCase() }).find { it.fileSystemName ==~ /eae-\w.\w/}
        String path = ctx.servletContext.getRealPath("web-app") + '/'+ eaeFileSystemName.fileSystemName.toString() + '/Scripts/';
        File scriptsFolder = new File(path);
        if(scriptsFolder.exists() && scriptsFolder.isDirectory()){
            File[] files = scriptsFolder.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    if(!file.canExecute()){
                        file.setExecutable(true)
                    }
                }
            }
        }

        println("Bootstrapping Completed. Scripts are executable.")
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
