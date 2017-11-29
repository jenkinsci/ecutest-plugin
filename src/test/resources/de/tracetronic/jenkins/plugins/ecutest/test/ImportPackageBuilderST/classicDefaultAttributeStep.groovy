node('windows') {
    step([$class: 'ImportPackageBuilder',
          importConfigs: [[$class: 'ImportPackageAttributeConfig', filePath: 'test.pkg']]])
}