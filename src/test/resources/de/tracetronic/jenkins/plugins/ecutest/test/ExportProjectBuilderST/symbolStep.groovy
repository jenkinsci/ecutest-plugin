node('windows') {
    exportPackages (
         exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg',
                          exportPath: 'export', createNewPath: false,
                          credentialsId: 'credentialsId', timeout: '600']]
    )
}