node('windows') {
    publishGenerators toolName: 'ecu.test',
        generators: [[name: 'HTML', settings: [], usePersistedSettings: true]],
        customGenerators: [[name: 'Custom', settings: []]],
        allowMissing: true, runOnFailed: true, archiving: false, keepAll: false
}
