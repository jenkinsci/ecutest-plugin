def strJobName = 'toolBuilderParam'
def strETInstall = 'ecu.test'
def strWorkspaceDir = 'test'
def strSettingsDir = 'settings'
def strTimeout = '60'
def intTimeout = 60
def bDebugMode = true
def bKeepInstance = true
def strToolLibsIni = 'C:\\ToolLibs.ini'
def intTcpPort = 5000

freeStyleJob("${strJobName}") {
    steps {
        startET("${strETInstall}") {
            workspaceDir("${strWorkspaceDir}")
            settingsDir("${strSettingsDir}")
            timeout("${strTimeout}")
            debugMode(bDebugMode)
            keepInstance(bKeepInstance)
        }
        startTS("${strETInstall}") {
            toolLibsIni("${strToolLibsIni}")
            tcpPort(intTcpPort)
            timeout("${strTimeout}")
            keepInstance(bKeepInstance)
        }
        stopET("${strETInstall}") {
            timeout(intTimeout)
        }
        stopTS("${strETInstall}") {
            timeout(intTimeout)
        }
    }
}
