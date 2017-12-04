node('windows') {
    importProjects (
        importConfigs: [[$class: 'ImportProjectArchiveConfig', tmsPath: 'test.prz']]
    )
}