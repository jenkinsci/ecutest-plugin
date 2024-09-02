def instance = ET.newInstallation('ecu.test', 'C:\\ecu.test')
assertDefaultInstance(instance)

instance = ET.newInstallation('ecu.test 2', 'C:\\ecu.test', 'ecu.test.Application.2024.1', 120, true)
assertInstance(instance)

instance = ET.newInstallation toolName: 'ecu.test 2', installPath: 'C:\\ecu.test',
                              progId: 'ecu.test.Application.2024.1', timeout: 120, registerComServer: true
assertInstance(instance)

def assertDefaultInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ecu.test'
    assert instance.installation.home == 'C:\\ecu.test'
    assert instance.installation.progId == 'ecu.test.Application'
    assert instance.installation.timeout == 0
    assert instance.installation.registerComServer == false
}

def assertInstance(instance) {
    assert instance
    assert instance.installation
    assert instance.installation.name == 'ecu.test 2'
    assert instance.installation.home == 'C:\\ecu.test'
    assert instance.installation.progId == 'ecu.test.Application.2024.1'
    assert instance.installation.registerComServer == true
}
