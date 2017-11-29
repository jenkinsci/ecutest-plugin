node('windows') {
    step([$class: 'ImportProjectBuilder',
          importConfigs: [[$class: 'ImportProjectDirConfig', tmsPath: 'projectDir']]])
}