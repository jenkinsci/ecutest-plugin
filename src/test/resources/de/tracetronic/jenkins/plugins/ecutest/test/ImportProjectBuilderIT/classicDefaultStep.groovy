node('windows') {
    step([$class: 'ImportProjectBuilder',
          importConfigs: [[$class: 'ImportProjectConfig', tmsPath: 'project']]])
}