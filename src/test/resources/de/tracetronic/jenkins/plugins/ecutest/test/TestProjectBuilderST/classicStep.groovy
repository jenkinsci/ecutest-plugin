node('windows') {
    step([$class: 'TestProjectBuilder',
          testFile: 'test.prj',
          testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],
          projectConfig: [execInCurrentPkgDir: true, filterExpression: 'Name="test"', jobExecMode: 'SEPARATE_SEQUENTIAL_EXECUTION'],
          executionConfig: [checkTestFile: false, stopOnError: false, timeout: '0']])
}