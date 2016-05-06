package smartR.plugin

import grails.util.Holders
import org.rosuda.REngine.Rserve.RConnection

class SessionManagerService {

    def SESSION_LIFETIME = 120 * 1000 * 60 // milliseconds
    def MAX_SESSIONS = 10
    def GROOVY_CHUNK_SIZE = 10 * 1024 * 1024 / 2 // should be the size for a string of 10MB
    def R_CHUNK_SIZE = 10 * 1024 * 1024 // should be the size for a string of about 10MB
    enum STATE {
        NULL,
        INIT,
        LOADING,
        LOADED,
        WORKING,
        COMPLETE,
        EXIT,
        ERROR
    }

    def sessions = [:]

    def setError(id, error) {
        sessions[id].msg = error
        sessions[id].state = STATE.ERROR
    }

    def getState(id) {
        try {
            return sessions[id].state.toString()
        } catch (all) {
            return STATE.NULL.toString()
        }
    }

    def getMsg(id) {
        sessions[id]?.msg
    }

    def sessionHasValidConnection(id) {
        def sane = false
        def thread = Thread.start {
            try {
                def connection = sessions[id].connection
                connection.assign("test1", "123")
                connection.voidEval("test2 <- test1")
                sane = connection.eval("test2").asString() == "123"
            } catch (Exception e) {
                sane = false
                println e.getMessage()
            }
        }
        thread.join(2000)
        return sane
    }

    def createFallbackErrorSession(id, error) {
        sessions[id] = [:]
        sessions[id].lifetime = Integer.MIN_VALUE
        setError(id, error)
    }

    def initSession(id, init) {
        if (init) {
            closeSession(id)
        }

        if (!init && !sessions[id]) {
            setError(id, 'No available session. This probably means that you were inactive for some time. Please restart the workflow.')
            return
        }

        if (!init && !sessionHasValidConnection(id)) {
            setError(id, 'You have an existing session but for some reason the Rserve connection is broken. Restart the workflow or Rserve.')
            return
        }

        if (!sessions[id]) {
            sessions[id] = [:]
        }

        sessions[id].lifetime = System.currentTimeMillis() + SESSION_LIFETIME
        sessions[id].msg = ''

        if (!sessions[id].connection) {
            try {
                sessions[id].connection = new RConnection(Holders.config.RModules.host, Holders.config.RModules.port)
                sessions[id].connection.stringEncoding = 'utf8'
            } catch (Exception e) {
                setError(id, 'Rserve refused the connection! Is it running?')
                println e.getMessage()
                return
            }
        }

        if (!sessionHasValidConnection(id)) {
            setError(id, 'At this point a valid Rserve connection should be established. Contact your administrator if you see this message.')
            return
        }

        if (sessions[id].connection.parseAndEval('try(if(!require(jsonlite)) stop(), silent=TRUE)').inherits("try-error")) {
            setError(id, 'R Package jsonlite is missing. Please go to https://github.com/sherzinger/SmartR and read the installation instructions.')
            return
        }

        removeExpiredSessions()
        while(sessions.length >= MAX_SESSIONS) closeOldestSession()
        assert sessions[id]

        sessions[id].state = STATE.INIT
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
            SmartR.data.cohort1 <- fromJSON(data_cohort1)
            SmartR.data.cohort2 <- fromJSON(data_cohort2)
        """)
    }

    def pushSettings(id, settings) {
        def connection = sessions[id].connection
        connection.assign("settings", settings)
        connection.voidEval("""
            SmartR.settings <- fromJSON(settings)
            SmartR.output <- list()
        """)
        sessions[id].state = STATE.LOADED
    }

    def runWorkflowScript(id, script) {
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
        if (sessionHasValidConnection(id)) sessions[id].connection.close()
        if (sessions[id]) sessions.remove(id)
    }

    def closeOldestSession() {
        closeSession(sessions.min { it.value.lifetime }.key)
    }

    def removeExpiredSessions() {
        def now = System.currentTimeMillis()
        sessions.findAll { it.value.lifetime < now }.each { closeSession(it.key) }
    }
}
