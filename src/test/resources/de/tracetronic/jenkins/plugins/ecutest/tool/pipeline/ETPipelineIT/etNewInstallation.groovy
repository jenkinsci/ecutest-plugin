def instance = ET.newInstallation('ECU-TEST', 'C:\\ECU-TEST')
assertDefaultInstance(instance)

instance = ET.newInstallation('ECU-TEST', 'C:\\ECU-TEST', 'ECU-TEST.Application.8.0', 120, true)
assertInstance(instance)

instance = ET.newInstallation toolName: 'ECU-TEST', installPath: 'C:\\ECU-TEST',
                              progId: 'ECU-TEST.Application.8.0', timeout: 120, registerComServer: true
assertInstance(instance)

def assertDefaultInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ECU-TEST'
    assert instance.installation.home == 'C:\\ECU-TEST'
    assert instance.installation.progId == 'ECU-TEST.Application'
    assert instance.installation.timeout == 0
    assert instance.installation.registerComServer == false
}

def assertInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ECU-TEST'
    assert instance.installation.home == 'C:\\ECU-TEST'
    assert instance.installation.progId == 'ECU-TEST.Application.8.0'
    assert instance.installation.registerComServer == true
}
