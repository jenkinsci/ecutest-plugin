node('windows') {
    step([$class: 'StopTSBuilder', toolName: 'ecu.test', timeout: '120'])
}
