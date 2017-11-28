node('windows') {
    publishATX atxName: 'TEST-GUIDE',
        allowMissing: true, runOnFailed: true,
        archiving: false, keepAll: false
}