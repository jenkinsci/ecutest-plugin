node('windows') {
    step([$class: 'ReportGeneratorPublisher', toolName: 'ECU-TEST'])
}