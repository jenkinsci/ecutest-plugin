node('windows') {
    importProjects (
         importConfigs: [[$class: 'ImportProjectDirConfig', 
                          tmsPath: 'projectDir', importPath: 'import', 
                          credentialsId: 'credentialsId', timeout: '600']]
    )
}