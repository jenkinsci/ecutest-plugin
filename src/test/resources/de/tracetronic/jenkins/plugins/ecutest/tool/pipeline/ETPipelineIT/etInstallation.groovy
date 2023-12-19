def instance = ET.installation('ecu.test')
assertInstance(instance)

instance = ET.installation toolName: 'ecu.test'
assertInstance(instance)

def assertInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ecu.test'
    assert instance.installation.home == 'C:\\ECU-TEST'
    assert instance.installation.progId == 'ECU-TEST.Application.8.0'
    assert instance.installation.timeout == 120
    assert instance.installation.registerComServer == false
}
