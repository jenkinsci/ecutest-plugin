node('windows') {
    importProjects (
        importConfigs: [[$class: 'ImportProjectDirConfig', tmsPath: 'projectDir']]
    )
}