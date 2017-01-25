def strJobName = 'toolBuilderParam'
def strETInstall = 'ECU-TEST'
def strWorkspaceDir = 'test'
def strSettingsDir = 'settings'
def strTimeout = '60'
def intTimeout = 60
def bDebugMode = true
def strToolLibsIni = 'C:\\ToolLibs.ini'
def intTcpPort = 5000

freeStyleJob("${strJobName}") {
    steps {
        startET("${strETInstall}") {
            workspaceDir("${strWorkspaceDir}")
            settingsDir("${strSettingsDir}")
            timeout("${strTimeout}")
            debugMode(bDebugMode)
        }
        startTS("${strETInstall}") {
            toolLibsIni("${strToolLibsIni}")
            tcpPort(intTcpPort)
            timeout("${strTimeout}")
        }
        stopET("${strETInstall}") {
            timeout(intTimeout)
        }
        stopTS("${strETInstall}") {
            timeout(intTimeout)
        }
    }
}
