node('windows') {
    step([$class: 'ATXPublisher', atxName: 'TEST-GUIDE'])
}