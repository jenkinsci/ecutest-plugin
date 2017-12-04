node('windows') {
    step([$class: 'TRFPublisher', allowMissing: true, archiving: false, keepAll: false, runOnFailed: true])
}