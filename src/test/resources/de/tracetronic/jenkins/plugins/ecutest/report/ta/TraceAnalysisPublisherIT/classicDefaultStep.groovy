node('windows') {
    step([$class: 'TraceAnalysisPublisher', toolName: 'ecu.test'])
}
