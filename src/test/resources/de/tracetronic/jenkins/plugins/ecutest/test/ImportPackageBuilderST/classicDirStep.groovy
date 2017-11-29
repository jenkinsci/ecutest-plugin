node('windows') {
    step([$class: 'ImportPackageBuilder',
          importConfigs: [[$class: 'ImportPackageDirConfig', 
                           tmsPath: 'packageDir', importPath: 'import', 
                           credentialsId: 'credentialsId', timeout: '600']]])
}