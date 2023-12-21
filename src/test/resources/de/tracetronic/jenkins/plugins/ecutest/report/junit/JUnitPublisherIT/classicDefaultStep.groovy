node('windows') {
    step([$class: 'JUnitPublisher', toolName: 'ecu.test'])
}
