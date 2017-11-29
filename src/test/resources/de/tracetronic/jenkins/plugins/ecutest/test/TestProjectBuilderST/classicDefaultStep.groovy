node('windows') {
    step([$class: 'TestProjectBuilder', testFile: 'test.prj'])
}