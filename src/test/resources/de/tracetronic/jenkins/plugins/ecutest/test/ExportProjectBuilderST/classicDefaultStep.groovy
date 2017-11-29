node('windows') {
    step([$class: 'ExportProjectBuilder',
          exportConfigs: [[$class: 'ExportProjectConfig', filePath: 'test.prj']]])
}