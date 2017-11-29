node('windows') {
    step([$class: 'ExportPackageBuilder',
          exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg']]])
}