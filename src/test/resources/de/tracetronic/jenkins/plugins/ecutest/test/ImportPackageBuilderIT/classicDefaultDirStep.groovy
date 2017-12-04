node('windows') {
    step([$class: 'ImportPackageBuilder',
          importConfigs: [[$class: 'ImportPackageDirConfig', tmsPath: 'packageDir']]])
}