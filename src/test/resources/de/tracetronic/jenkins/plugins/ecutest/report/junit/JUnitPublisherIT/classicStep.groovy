node('windows') {
    step([$class: 'JUnitPublisher', toolName: 'ecu.test',
            unstableThreshold: 0, failedThreshold: 0,
            allowMissing: true, runOnFailed: true])
}
