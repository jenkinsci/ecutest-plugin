/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
node('windows') {
    publishTraceAnalysis toolName: 'ecu.test',
        timeout: '600', mergeReports: true, createReportDir: true,
        allowMissing: true, runOnFailed: true
}
