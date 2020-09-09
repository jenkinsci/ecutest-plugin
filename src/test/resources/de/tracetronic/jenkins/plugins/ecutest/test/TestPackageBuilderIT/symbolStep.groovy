/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
node('windows') {
    testPackage testFile: 'test.pkg',
                testConfig: [constants: [], forceReload: true, loadOnly: true, tbcFile: 'test.tbc', tcfFile: 'test.tcf'],
                packageConfig: [outputParameters: [], parameters: [], runTest: false, runTraceAnalysis: false],
                executionConfig: [checkTestFile: false, recordWarnings: true, stopOnError: false, timeout: '0']
}