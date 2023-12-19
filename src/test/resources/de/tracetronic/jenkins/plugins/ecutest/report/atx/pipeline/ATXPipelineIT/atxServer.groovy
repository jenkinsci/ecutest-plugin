def server = ATX.server('test.guide')
assertSettings(server)

server = ATX.server atxName: 'test.guide'
assertSettings(server)

def assertSettings(server) {
    assert server.getSetting('uploadToServer').value == false
    assert server.getSetting('serverURL').value == '127.0.0.1'
    assert server.getSettings()['uploadToServer'] == false
    assert server.getSettings()['serverURL'] == '127.0.0.1'
}
