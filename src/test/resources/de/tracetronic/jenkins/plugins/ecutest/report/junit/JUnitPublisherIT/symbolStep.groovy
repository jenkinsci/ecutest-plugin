node('windows') {
    publishUNIT toolName: 'ecu.test',
        unstableThreshold: 0, failedThreshold: 0,
        allowMissing: true, runOnFailed: true
}
