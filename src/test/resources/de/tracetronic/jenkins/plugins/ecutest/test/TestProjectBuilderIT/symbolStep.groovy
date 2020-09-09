/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
node('windows') {
    testProject testFile: 'test.prj',
                testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],
                projectConfig: [execInCurrentPkgDir: true, filterExpression: 'Name="test"', jobExecMode: 'SEPARATE_SEQUENTIAL_EXECUTION'],
                executionConfig: [checkTestFile: false, recordWarnings: true, stopOnError: false, timeout: '0']
}