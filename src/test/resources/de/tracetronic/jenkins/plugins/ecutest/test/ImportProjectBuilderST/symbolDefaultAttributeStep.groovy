node('windows') {
    importProjects (
        importConfigs: [[$class: 'ImportProjectAttributeConfig', filePath: 'test.prj']]
    )
}