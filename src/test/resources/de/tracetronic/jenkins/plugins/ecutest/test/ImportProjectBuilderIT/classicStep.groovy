node('windows') {
    step([$class: 'ImportProjectBuilder',
          importConfigs: [[$class: 'ImportProjectConfig', 
                           tmsPath: 'project', importPath: 'import', 
                           credentialsId: 'credentialsId', timeout: '600']]])
}