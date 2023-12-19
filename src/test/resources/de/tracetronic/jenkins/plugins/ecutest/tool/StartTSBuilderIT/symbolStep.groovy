node('windows') {
    writeFile file: 'ToolLibs.ini', text: ''
    startTS toolName: 'ecu.test',
            toolLibsIni: pwd() + '\\ToolLibs.ini',
            tcpPort: '5017', timeout: '120', keepInstance: true
}
