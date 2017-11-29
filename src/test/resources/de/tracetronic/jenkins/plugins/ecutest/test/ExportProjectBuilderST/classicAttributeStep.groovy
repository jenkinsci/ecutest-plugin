node('windows') {
    step([$class: 'ExportProjectBuilder',
          exportConfigs: [[$class: 'ExportProjectAttributeConfig', filePath: 'test.prj',
                           credentialsId: 'credentialsId', timeout: '600']]])
}