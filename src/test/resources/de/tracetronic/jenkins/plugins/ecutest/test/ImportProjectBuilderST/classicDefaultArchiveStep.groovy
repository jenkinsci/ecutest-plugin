node('windows') {
    step([$class: 'ImportProjectBuilder',
          importConfigs: [[$class: 'ImportProjectArchiveConfig', tmsPath: 'test.prz']]])
}