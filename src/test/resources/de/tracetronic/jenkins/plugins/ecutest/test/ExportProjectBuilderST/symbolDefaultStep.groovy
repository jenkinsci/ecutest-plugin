node('windows') {
    exportPackages (
        exportConfigs: [[$class: 'ExportPackageConfig', filePath: 'test.pkg']]
    )
}