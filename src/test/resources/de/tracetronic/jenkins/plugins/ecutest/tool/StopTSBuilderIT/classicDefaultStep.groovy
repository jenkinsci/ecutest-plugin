node('windows') {
    step([$class: 'StopTSBuilder', toolName: 'ecu.test'])
}
