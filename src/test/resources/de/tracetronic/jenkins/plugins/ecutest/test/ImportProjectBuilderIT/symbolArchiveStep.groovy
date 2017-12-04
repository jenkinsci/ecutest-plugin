node('windows') {
    importProjects (
         importConfigs: [[$class: 'ImportProjectArchiveConfig', 
                          tmsPath: 'test.prz', importPath: 'import', 
                          importConfigPath: 'import', replaceFiles: true]]
    )
}