/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
node('windows') {
    publishATX atxName: 'test.guide',
        failOnOffline: true, usePersistedSettings: true, injectBuildVars: true,
        allowMissing: true, runOnFailed: true,
        archiving: false, keepAll: false
}
