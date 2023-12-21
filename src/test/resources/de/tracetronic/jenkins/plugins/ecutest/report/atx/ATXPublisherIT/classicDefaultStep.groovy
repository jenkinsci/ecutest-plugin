node('windows') {
    step([$class: 'ATXPublisher', atxName: 'test.guide'])
}