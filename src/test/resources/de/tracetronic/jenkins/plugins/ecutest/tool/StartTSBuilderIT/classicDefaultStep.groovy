node('windows') {
    step([$class: 'StartTSBuilder', toolName: 'ecu.test'])
}
