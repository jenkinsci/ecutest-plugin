node('windows') {
    step([$class: 'TestFolderBuilder', testFile: 'tests'])
}