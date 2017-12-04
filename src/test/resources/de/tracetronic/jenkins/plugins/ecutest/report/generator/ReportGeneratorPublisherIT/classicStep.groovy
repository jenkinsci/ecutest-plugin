node('windows') {
    step([$class: 'ReportGeneratorPublisher', toolName: 'ECU-TEST',
          generators: [[name: 'HTML', settings: []]], customGenerators: [[name: 'Custom', settings: []]],
          allowMissing: true, archiving: false, keepAll: false, runOnFailed: true])
}