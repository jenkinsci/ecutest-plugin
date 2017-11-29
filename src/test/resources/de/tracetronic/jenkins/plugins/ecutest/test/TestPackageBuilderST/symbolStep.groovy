node('windows') {
    testPackage testFile: 'test.pkg',
                testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],
                packageConfig: [parameters: [], runTest: false, runTraceAnalysis: false],
                executionConfig: [checkTestFile: false, stopOnError: false, timeout: '0']
}