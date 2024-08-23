def instance = ET.installation('ecu.test')
assertInstance(instance)

instance = ET.installation toolName: 'ecu.test'
assertInstance(instance)

def assertInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ecu.test'
    assert instance.installation.home == 'C:\\ecu.test'
    assert instance.installation.progId == 'ecu.test.Application.2024.1'
    assert instance.installation.timeout == 120
    assert instance.installation.registerComServer == false
}
