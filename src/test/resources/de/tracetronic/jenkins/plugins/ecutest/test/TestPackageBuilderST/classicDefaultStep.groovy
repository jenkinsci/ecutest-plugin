node('windows') {
    step([$class: 'TestPackageBuilder', testFile: 'test.pkg'])
}