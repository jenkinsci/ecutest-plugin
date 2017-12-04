node('windows') {
    step([$class: 'ImportProjectBuilder',
          importConfigs: [[$class: 'ImportProjectAttributeConfig', filePath: 'test.prj']]])
}