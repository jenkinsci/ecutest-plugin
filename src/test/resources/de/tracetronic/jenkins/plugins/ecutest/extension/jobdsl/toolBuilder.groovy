freeStyleJob("toolBuilder") {
    steps {
        startET("ECU-TEST") {
            workspaceDir("test")
            settingsDir("settings")
            timeout("60")
            debugMode(true)
            keepInstance(true)
        }
        startTS("ECU-TEST") {
            toolLibsIni("C:\\ToolLibs.ini")
            tcpPort(5000)
            timeout("60")
            keepInstance(true)
        }
        stopET("ECU-TEST") {
            timeout(60)
        }
        stopTS("ECU-TEST") {
            timeout(60)
        }
    }
}
