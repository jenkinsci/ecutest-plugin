node('windows') {
    step([$class: 'StartTSBuilder', toolName: 'ECU-TEST'])
}