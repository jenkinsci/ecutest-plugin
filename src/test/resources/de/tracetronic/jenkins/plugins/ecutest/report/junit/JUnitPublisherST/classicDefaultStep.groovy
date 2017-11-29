node('windows') {
    step([$class: 'JUnitPublisher', toolName: 'ECU-TEST'])
}