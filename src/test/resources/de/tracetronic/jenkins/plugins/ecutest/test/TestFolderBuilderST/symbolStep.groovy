node('windows') {
    testFolder testFile: 'tests', recursiveScan: true, scanMode: 'PACKAGES_ONLY',
               testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],
               packageConfig: [parameters: [], runTest: false, runTraceAnalysis: false],
               projectConfig: [execInCurrentPkgDir: true, filterExpression: 'Name="test"', jobExecMode: 'SEPARATE_SEQUENTIAL_EXECUTION'],
               executionConfig: [checkTestFile: false, stopOnError: false, timeout: '0']
}