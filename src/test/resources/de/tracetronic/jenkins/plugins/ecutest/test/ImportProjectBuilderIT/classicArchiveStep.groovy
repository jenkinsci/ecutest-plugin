node('windows') {
    step([$class: 'ImportProjectBuilder',
          importConfigs: [[$class: 'ImportProjectArchiveConfig', 
                           tmsPath: 'test.prz', importPath: 'import', 
                           importConfigPath: 'import', replaceFiles: true]]])
}