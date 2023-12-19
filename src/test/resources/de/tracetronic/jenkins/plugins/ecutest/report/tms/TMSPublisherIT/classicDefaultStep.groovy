node('windows') {
    step([$class: 'TMSPublisher', toolName: 'ecu.test', credentialsId: 'credentialsId'])
}
