/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
node('windows') {
    testFolder testFile: 'tests', recursiveScan: true, scanMode: 'PACKAGES_ONLY', failFast: false,
               testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],
               packageConfig: [parameters: [], runTest: false, runTraceAnalysis: false],
               projectConfig: [execInCurrentPkgDir: true, filterExpression: 'Name="test"', jobExecMode: 'SEPARATE_SEQUENTIAL_EXECUTION'],
               executionConfig: [checkTestFile: false, recordWarnings: true, stopOnError: false, timeout: '0']
}
