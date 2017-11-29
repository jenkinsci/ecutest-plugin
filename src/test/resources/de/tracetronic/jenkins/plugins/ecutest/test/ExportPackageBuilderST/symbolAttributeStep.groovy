node('windows') {
    exportPackages (
         exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg',
                          credentialsId: 'credentialsId', timeout: '600']]
    )
}