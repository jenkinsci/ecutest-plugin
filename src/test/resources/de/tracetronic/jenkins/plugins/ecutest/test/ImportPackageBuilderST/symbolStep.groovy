node('windows') {
    importPackages (
         importConfigs: [[$class: 'ImportPackageConfig', 
                          tmsPath: 'package', importPath: 'import', 
                          credentialsId: 'credentialsId', timeout: '600']]
    )
}