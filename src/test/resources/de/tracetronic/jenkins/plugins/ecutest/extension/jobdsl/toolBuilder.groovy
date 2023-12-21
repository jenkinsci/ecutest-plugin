freeStyleJob("toolBuilder") {
    steps {
        startET("ecu.test") {
            workspaceDir("test")
            settingsDir("settings")
            timeout("60")
            debugMode(true)
            keepInstance(true)
        }
        startTS("ecu.test") {
            toolLibsIni("C:\\ToolLibs.ini")
            tcpPort(5000)
            timeout("60")
            keepInstance(true)
        }
        stopET("ecu.test") {
            timeout(60)
        }
        stopTS("ecu.test") {
            timeout(60)
        }
    }
}
