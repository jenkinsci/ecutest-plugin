node('windows') {
    importProjects (
         importConfigs: [[$class: 'ImportProjectAttributeConfig', filePath: 'test.prj',
                          credentialsId: 'credentialsId', timeout: '600']]
    )
}