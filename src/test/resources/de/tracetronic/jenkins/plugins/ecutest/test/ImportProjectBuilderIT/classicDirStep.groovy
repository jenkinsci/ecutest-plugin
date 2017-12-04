node('windows') {
    step([$class: 'ImportProjectBuilder',
          importConfigs: [[$class: 'ImportProjectDirConfig', 
                           tmsPath: 'projectDir', importPath: 'import', 
                           credentialsId: 'credentialsId', timeout: '600']]])
}