/*
 * Copyright (c) 2015-2024 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.pipeline.ATXNewServerStepIT


server = newATXServer atxName: 'test.guide2', toolName: 'ecu.test',
    config: atxConfig(
        settings: [
            atxTextSetting(group: 'CONNECTION', name: 'serverURL', value: 'localhorst'),
            atxTextSetting(group: 'CONNECTION', name: 'serverPort', value: '4242'),
            atxTextSetting(group: 'CONNECTION', name: 'projectId', value: '21'),
            atxBooleanSetting(group: 'UPLOAD', name: 'uploadToServer', value: true)
        ],
        customSettings: [
            atxCustomTextSetting(name: 'someCustomConst', value: 'theValue'),
            atxCustomBooleanSetting(name: 'someBoolValue', checked: true)
        ]
    )
assertSettings(server)

def assertSettings(server) {
    assert server.installation.name == 'test.guide2'
    assert server.installation.toolName == 'ecu.test'

    assert server.getSetting('uploadToServer').value == true
    assert server.getSetting('serverURL').value == 'localhorst'
    assert server.getSetting('serverPort').value == '4242'
    assert server.getSetting('useHttpsConnection').value == false
    assert server.getSetting('serverContextPath').value == ''
    assert server.getSetting('uploadAuthenticationKey').secretValue == ''
    assert server.getSetting('projectId').value == '21'

    assert server.getCustomSetting('someCustomConst').value == 'theValue'
    assert server.getCustomSetting('someBoolValue').checked
    assert server.getCustomSetting('unknown') == null
}
