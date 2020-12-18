/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
def server = ATX.server('TEST-GUIDE')
assert server.getSetting('uploadToServer').value == false

server.overrideSetting('uploadToServer', true)
assert server.getSetting('uploadToServer').value == true

server.overrideSetting('uploadAuthenticationKey', 'auth-123')
assert server.getSetting('uploadAuthenticationKey').secretValue == 'auth-123'

def settings = [serverURL: 'test.abc', useHttpsConnection: true]
server.overrideSettings(settings)
assert server.getSetting('serverURL').value == 'test.abc'
assert server.getSetting('useHttpsConnection').value == true
