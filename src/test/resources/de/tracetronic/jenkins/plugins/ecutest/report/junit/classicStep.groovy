node('windows') {
    step([$class: 'JUnitPublisher', toolName: 'ECU-TEST',
            unstableThreshold: 0, failedThreshold: 0,
            allowMissing: true, runOnFailed: true])
}