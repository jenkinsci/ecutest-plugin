/*
 * Copyright (c) 2015-2021 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
node('windows') {
    step([$class      : 'TraceAnalysisPublisher', toolName: 'ECU-TEST',
          timeout     : '600', mergeReports: true, createReportDir: true,
          allowMissing: true, runOnFailed: true])
}