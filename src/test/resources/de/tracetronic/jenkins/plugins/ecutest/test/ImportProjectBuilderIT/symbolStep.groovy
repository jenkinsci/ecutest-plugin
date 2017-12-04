node('windows') {
    importProjects (
         importConfigs: [[$class: 'ImportProjectConfig', 
                          tmsPath: 'project', importPath: 'import', 
                          credentialsId: 'credentialsId', timeout: '600']]
    )
}