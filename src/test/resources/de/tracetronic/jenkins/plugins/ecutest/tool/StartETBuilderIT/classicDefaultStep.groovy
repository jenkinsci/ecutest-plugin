node('windows') {
    step([$class: 'StartETBuilder', toolName: 'ecu.test'])
}
