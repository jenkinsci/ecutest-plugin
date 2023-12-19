/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
def server = ATX.newServer('test.guide', 'ecu.test')
assertDefaultSettings(server)

server = ATX.newServer('CUSTOM-test.guide', 'CUSTOM-ecu.test', 'https://test.abc:443/test', true, 'auth-123', '42')
assertCustomSettings(server)

server = ATX.newServer atxName: 'CUSTOM-test.guide', toolName: 'CUSTOM-ecu.test',
    fullServerURL: 'https://test.abc:443/test', uploadToServer: true,
    uploadAuthenticationKey: 'auth-123', projectId: '42'
assertCustomSettings(server)

def assertDefaultSettings(server) {
    assert server.installation.name == 'test.guide'
    assert server.installation.toolName == 'ecu.test'

    assert server.getSetting('uploadToServer').value == false
    assert server.getSetting('serverURL').value == '127.0.0.1'
    assert server.getSetting('serverPort').value == '8085'
    assert server.getSetting('useHttpsConnection').value == false
    assert server.getSetting('serverContextPath').value == ''
    assert server.getSetting('uploadAuthenticationKey').secretValue == ''
    assert server.getSetting('projectId').value == '1'
}

def assertCustomSettings(server) {
    assert server.installation.name == 'CUSTOM-test.guide'
    assert server.installation.toolName == 'CUSTOM-ecu.test'

    assert server.getSetting('uploadToServer').value == true
    assert server.getSetting('serverURL').value == 'test.abc'
    assert server.getSetting('serverPort').value == '443'
    assert server.getSetting('useHttpsConnection').value == true
    assert server.getSetting('serverContextPath').value == 'test'
    assert server.getSetting('uploadAuthenticationKey').secretValue == 'auth-123'
    assert server.getSetting('projectId').value == '42'
}
