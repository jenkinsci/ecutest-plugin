node('windows') {
    step([$class: 'ExportPackageBuilder',
          exportConfigs: [[$class: 'ExportPackageAttributeConfig', filePath: 'test.pkg',
                           credentialsId: 'credentialsId', timeout: '600']]])
}