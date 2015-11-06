freeStyleJob("toolBuilder") {
    steps {
        startET("ECU-TEST") {
            workspaceDir("test")
            timeout("60")
            debugMode(true)
        }
        startTS("ECU-TEST") {
            toolLibsIni("C:\\ToolLibs.ini")
            tcpPort(5000)
            timeout("60")
        }
        stopET("ECU-TEST") {
            timeout(60)
        }
        stopTS("ECU-TEST") {
            timeout(60)
        }
    }
}
