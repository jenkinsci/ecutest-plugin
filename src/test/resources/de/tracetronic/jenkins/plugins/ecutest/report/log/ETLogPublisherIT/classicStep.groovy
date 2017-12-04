node('windows') {
    step([$class: 'ETLogPublisher', testSpecific: true,
            unstableOnWarning: true, failedOnError: true,
            allowMissing: true, runOnFailed: true, archiving: false, keepAll: false])
}