def instance = ET.newInstallation('ecu.test', 'C:\\ECU-TEST')
assertDefaultInstance(instance)

instance = ET.newInstallation('ecu.test', 'C:\\ECU-TEST', 'ECU-TEST.Application.8.0', 120, true)
assertInstance(instance)

instance = ET.newInstallation toolName: 'ecu.test', installPath: 'C:\\ECU-TEST',
                              progId: 'ECU-TEST.Application.8.0', timeout: 120, registerComServer: true
assertInstance(instance)

def assertDefaultInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ecu.test'
    assert instance.installation.home == 'C:\\ECU-TEST'
    assert instance.installation.progId == 'ECU-TEST.Application'
    assert instance.installation.timeout == 0
    assert instance.installation.registerComServer == false
}

def assertInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ecu.test'
    assert instance.installation.home == 'C:\\ECU-TEST'
    assert instance.installation.progId == 'ECU-TEST.Application.8.0'
    assert instance.installation.registerComServer == true
}
