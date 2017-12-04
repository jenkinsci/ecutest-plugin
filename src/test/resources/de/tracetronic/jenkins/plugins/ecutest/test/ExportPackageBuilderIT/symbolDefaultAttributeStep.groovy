node('windows') {
    exportPackages (
        exportConfigs: [[$class: 'ExportPackageAttributeConfig', filePath: 'test.pkg']]
    )
}