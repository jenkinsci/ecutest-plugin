node('windows') {
    publishTMS toolName: 'ECU-TEST', 
        credentialsId: 'credentialsId', timeout: '600',
        allowMissing: true, runOnFailed: true
}