node('windows') {
    step([$class: 'ImportPackageBuilder',
          importConfigs: [[$class: 'ImportPackageConfig', 
                           tmsPath: 'package', importPath: 'import', 
                           credentialsId: 'credentialsId', timeout: '600']]])
}