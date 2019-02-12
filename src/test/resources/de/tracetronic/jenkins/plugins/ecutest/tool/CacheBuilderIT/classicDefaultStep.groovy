node('windows') {
    step([$class: 'CacheBuilder', caches: [[$class: 'CacheConfig', type: 'A2L', filePath: 'C:\\test.a2l']]])
}
