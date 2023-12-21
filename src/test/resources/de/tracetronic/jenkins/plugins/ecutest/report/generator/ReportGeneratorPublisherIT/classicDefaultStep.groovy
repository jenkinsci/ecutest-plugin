node('windows') {
    step([$class: 'ReportGeneratorPublisher', toolName: 'ecu.test'])
}
