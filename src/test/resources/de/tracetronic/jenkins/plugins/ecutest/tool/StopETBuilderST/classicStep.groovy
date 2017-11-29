node('windows') {
    step([$class: 'StopETBuilder', toolName: 'ECU-TEST', timeout: '120'])
}