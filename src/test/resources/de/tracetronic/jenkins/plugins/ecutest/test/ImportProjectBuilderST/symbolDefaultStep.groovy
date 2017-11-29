node('windows') {
    importProjects (
        importConfigs: [[$class: 'ImportProjectConfig', tmsPath: 'project']]
    )
}