node('windows') {
    importPackages (
         importConfigs: [[$class: 'ImportPackageDirConfig', 
                          tmsPath: 'packageDir', importPath: 'import', 
                          credentialsId: 'credentialsId', timeout: '600']]
    )
}