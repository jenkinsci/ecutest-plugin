node('windows') {
    publishGenerators toolName: 'ECU-TEST',
        generators: [[name: 'HTML', settings: []]], customGenerators: [[name: 'Custom', settings: []]],
        allowMissing: true, runOnFailed: true, archiving: false, keepAll: false
}