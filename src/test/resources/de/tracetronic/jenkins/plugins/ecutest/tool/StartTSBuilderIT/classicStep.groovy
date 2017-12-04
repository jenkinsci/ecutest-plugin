node('windows') {
    writeFile file: 'ToolLibs.ini', text: ''
    step([$class: 'StartTSBuilder', toolName: 'ECU-TEST',
          toolLibsIni: pwd() + '\\ToolLibs.ini',
          tcpPort: '5017', timeout: '120', keepInstance: true])
}