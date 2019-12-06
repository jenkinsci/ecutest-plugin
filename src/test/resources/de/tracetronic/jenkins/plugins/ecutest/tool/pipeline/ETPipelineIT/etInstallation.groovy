def instance = ET.installation('ECU-TEST')
assertInstance(instance)

instance = ET.installation toolName: 'ECU-TEST'
assertInstance(instance)

def assertInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ECU-TEST'
    assert instance.installation.home == 'C:\\ECU-TEST'
    assert instance.installation.progId == 'ECU-TEST.Application.8.0'
    assert instance.installation.timeout == 120
    assert instance.installation.registerComServer == false
}
