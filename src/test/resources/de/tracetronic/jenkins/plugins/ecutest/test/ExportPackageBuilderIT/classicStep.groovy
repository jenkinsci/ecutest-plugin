node('windows') {
    step([$class: 'ExportPackageBuilder',
          exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg',
                           exportPath: 'export', createNewPath: false,
                           credentialsId: 'credentialsId', timeout: '600']]])
}