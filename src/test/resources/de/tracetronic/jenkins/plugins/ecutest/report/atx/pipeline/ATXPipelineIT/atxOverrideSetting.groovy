def server = ATX.server('TEST-GUIDE')
assert server.getSetting('uploadToServer').value == false

server.overrideSetting('uploadToServer', true)
assert server.getSetting('uploadToServer').value == true

def settings = [serverURL: 'test.abc', useHttpsConnection: true]
server.overrideSettings(settings)
assert server.getSetting('serverURL').value == 'test.abc'
assert server.getSetting('useHttpsConnection').value == true
