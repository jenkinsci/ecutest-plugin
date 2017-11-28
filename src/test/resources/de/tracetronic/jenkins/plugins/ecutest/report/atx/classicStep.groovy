node('windows') {
    step([$class: 'ATXPublisher', atxName: 'TEST-GUIDE', 
            allowMissing: true, runOnFailed: true, 
            archiving: false, keepAll: false])
}