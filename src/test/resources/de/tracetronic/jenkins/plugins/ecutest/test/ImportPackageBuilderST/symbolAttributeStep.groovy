node('windows') {
    importPackages (
         importConfigs: [[$class: 'ImportPackageAttributeConfig', filePath: 'test.pkg',
                          credentialsId: 'credentialsId', timeout: '600']]
    )
}