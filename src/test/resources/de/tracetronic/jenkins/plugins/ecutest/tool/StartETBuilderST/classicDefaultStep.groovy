node('windows') {
    step([$class: 'StartETBuilder', toolName: 'ECU-TEST'])
}