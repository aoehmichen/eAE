package smartR.plugin

import grails.util.Holders
import org.rosuda.REngine.Rserve.RConnection

class SessionManagerService {

    def SESSION_LIFETIME = 120 * 1000 * 60 // milliseconds
    def MAX_SESSIONS = 10
    def GROOVY_CHUNK_SIZE = 10 * 1024 * 1024 / 2 // should be the size for a string of 10MB
    def R_CHUNK_SIZE = 10 * 1024 * 1024 // should be the size for a string of about 10MB
    enum STATE {
        INIT,
        LOADING,
        LOADED,
        WORKING,
        COMPLETE,
        ERROR
    }

    def sessions = [:]

    def setError(id, error) {
        sessions[id].msg = error
        sessions[id].state = STATE.ERROR
    }

    def getState(id) {
        sessions[id]?.state?.toString()
    }

    def getMsg(id) {
        sessions[id]?.msg
    }

    def sessionValid(id) {
        try {
            def connection = sessions[id].connection
            connection.assign("test1", "123")
            connection.voidEval("test2 <- test1")
            return connection.eval("test2").asString() == "123"
        } catch (all) {
            return false
        }
    }

    def initSession(id, init) {
        if (init && sessions[id]) closeSession(id)
        if (!sessions[id]) sessions[id] = [:]
        sessions[id].lifetime = System.currentTimeMillis() + SESSION_LIFETIME
        sessions[id].msg = ''
        if (!sessionValid(id)) {
            try {
                sessions[id].connection = new RConnection(Holders.config.RModules.host, Holders.config.RModules.port)
                sessions[id].connection.stringEncoding = 'utf8'
            } catch (all) { }
        }
        if (!sessionValid(id)) {
            setError(id, 'Rserve refused the connection! Is it running?')
        } else if (sessions[id].connection.parseAndEval('try(if(!require(jsonlite)) stop(), silent=TRUE)').inherits("try-error")) {
            setError(id, 'R Package jsonlite is missing. Please go to https://github.com/sherzinger/SmartR and read the installation instructions.')
        } else {
            sessions[id].state = STATE.INIT
        }
        removeExpiredSessions()
        while(sessions.length >= MAX_SESSIONS) closeOldestSession()
    }

    def pushData(id, data) {
        def connection = sessions[id].connection
        def dataString1 = data.cohort1.toString()
        def dataString2 = data.cohort2.toString()

        def dataPackages1 = dataString1.split("(?<=\\G.{${GROOVY_CHUNK_SIZE}})")
        def dataPackages2 = dataString2.split("(?<=\\G.{${GROOVY_CHUNK_SIZE}})")

        connection.eval("data_cohort1 <- ''")
        connection.eval("data_cohort2 <- ''")

        dataPackages1.each { chunk ->
            connection.assign("chunk", chunk)
            connection.voidEval("data_cohort1 <- paste(data_cohort1, chunk, sep='')")
        }

        dataPackages2.each { chunk ->
            connection.assign("chunk", chunk)
            connection.voidEval("data_cohort2 <- paste(data_cohort2, chunk, sep='')")
        }

        connection.voidEval("""
            require(jsonlite)
            SmartR.data.cohort1 <- fromJSON(data_cohort1)
            SmartR.data.cohort2 <- fromJSON(data_cohort2)
        """)
    }

    def pushSettings(id, settings) {
        def connection = sessions[id].connection
        connection.assign("settings", settings)
        connection.voidEval("""
            require(jsonlite)
            SmartR.settings <- fromJSON(settings)
            SmartR.output <- list()
        """)
        sessions[id].state = STATE.LOADED
    }

    def runWorkflowScript(id, script) {
        sessions[id].state = STATE.WORKING
        def connection = sessions[id].connection
        def scriptCommand = "source('${script}')".replace("\\", "/")
        try {
            def ret = connection.parseAndEval("try(${scriptCommand}, silent=TRUE)")
            if (ret.inherits("try-error")) {
                setError(id, ret.asString())
            } else {
                sessions[id].state = STATE.COMPLETE
            }
        } catch (all) {
            setError(id, 'Connection hiccup. If this happens often contact your administrator.')
        }
    }

    def pullData(id) {
        def connection = sessions[id].connection
        connection.voidEval("""
            json <- toString(toJSON(SmartR.output, digits=5))
            start <- 1
            stop <- ${R_CHUNK_SIZE}""")
        def json = ''
        def chunk = ''
        while(chunk = connection.eval("""
            chunk <- substr(json, start, stop)
            start <- stop + 1
            stop <- stop + ${R_CHUNK_SIZE}
            chunk""").asString()) {
            json += chunk
        }
        json
    }

    def closeSession(id) {
        if (sessionValid(id)) sessions[id].connection.close()
        sessions.remove(id)
    }

    def closeOldestSession() {
        closeSession(sessions.min { it.value.lifetime }.key)
    }

    def removeExpiredSessions() {
        def now = System.currentTimeMillis()
        sessions.findAll { it.value.lifetime < now }.each { closeSession(it.key) }
    }
}
