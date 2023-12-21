node('windows') {
    step([$class: 'StopETBuilder', toolName: 'ecu.test'])
}
