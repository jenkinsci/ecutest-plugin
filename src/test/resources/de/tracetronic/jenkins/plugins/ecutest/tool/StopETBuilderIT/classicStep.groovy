node('windows') {
    step([$class: 'StopETBuilder', toolName: 'ecu.test', timeout: '120'])
}
