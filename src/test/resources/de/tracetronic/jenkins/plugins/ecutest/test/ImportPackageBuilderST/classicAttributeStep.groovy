node('windows') {
    step([$class: 'ImportPackageBuilder',
          importConfigs: [[$class: 'ImportPackageAttributeConfig', filePath: 'test.pkg',
                           credentialsId: 'credentialsId', timeout: '600']]])
}