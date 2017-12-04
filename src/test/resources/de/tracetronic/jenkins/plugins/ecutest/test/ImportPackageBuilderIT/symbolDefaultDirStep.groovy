node('windows') {
    importPackages (
        importConfigs: [[$class: 'ImportPackageDirConfig', tmsPath: 'packageDir']]
    )
}