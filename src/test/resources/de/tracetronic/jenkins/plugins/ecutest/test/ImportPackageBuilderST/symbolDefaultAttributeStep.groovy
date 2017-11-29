node('windows') {
    importPackages (
        importConfigs: [[$class: 'ImportPackageAttributeConfig', filePath: 'test.pkg']]
    )
}