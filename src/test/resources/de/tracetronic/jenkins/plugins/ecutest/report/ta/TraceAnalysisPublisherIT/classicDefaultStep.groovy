node('windows') {
    step([$class: 'TraceAnalysisPublisher', toolName: 'ECU-TEST'])
}