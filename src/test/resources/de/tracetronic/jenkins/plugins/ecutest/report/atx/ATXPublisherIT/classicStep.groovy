node('windows') {
    step([$class: 'ATXPublisher', atxName: 'TEST-GUIDE', failOnOffline: true,
            allowMissing: true, runOnFailed: true,
            archiving: false, keepAll: false])
}
