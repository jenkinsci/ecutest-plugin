/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
node('windows') {
    publishATX atxName: 'TEST-GUIDE',
        failOnOffline: true, usePersistedSettings: true, injectBuildVars: true,
        allowMissing: true, runOnFailed: true,
        archiving: false, keepAll: false
}
