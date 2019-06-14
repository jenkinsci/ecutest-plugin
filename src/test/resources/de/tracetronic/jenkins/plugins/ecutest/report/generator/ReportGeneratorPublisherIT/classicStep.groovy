node('windows') {
    step([$class: 'ReportGeneratorPublisher', toolName: 'ECU-TEST',
          generators: [[name: 'HTML', settings: [], usePersistedSettings: true]],
          customGenerators: [[name: 'Custom', settings: []]],
          allowMissing: true, archiving: false, keepAll: false, runOnFailed: true])
}
