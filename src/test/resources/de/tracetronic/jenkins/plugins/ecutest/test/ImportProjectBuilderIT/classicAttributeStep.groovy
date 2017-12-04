node('windows') {
    step([$class: 'ImportProjectBuilder',
          importConfigs: [[$class: 'ImportProjectAttributeConfig', filePath: 'test.prj',
                           credentialsId: 'credentialsId', timeout: '600']]])
}