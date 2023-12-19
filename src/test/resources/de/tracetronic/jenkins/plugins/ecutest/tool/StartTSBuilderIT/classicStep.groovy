node('windows') {
    writeFile file: 'ToolLibs.ini', text: ''
    step([$class: 'StartTSBuilder', toolName: 'ecu.test',
          toolLibsIni: pwd() + '\\ToolLibs.ini',
          tcpPort: '5017', timeout: '120', keepInstance: true])
}
