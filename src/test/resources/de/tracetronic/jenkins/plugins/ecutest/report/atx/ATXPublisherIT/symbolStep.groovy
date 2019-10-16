node('windows') {
    publishATX atxName: 'TEST-GUIDE', failOnOffline: true,
        allowMissing: true, runOnFailed: true,
        archiving: false, keepAll: false
}
