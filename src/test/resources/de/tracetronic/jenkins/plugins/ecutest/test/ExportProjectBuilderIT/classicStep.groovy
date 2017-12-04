node('windows') {
    step([$class: 'ExportProjectBuilder',
          exportConfigs: [[$class: 'ExportProjectConfig', filePath: 'test.prj',
                           exportPath: 'export', createNewPath: false,
                           credentialsId: 'credentialsId', timeout: '600']]])
}