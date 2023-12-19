node('windows') {
    startET toolName: 'ecu.test',
            workspaceDir: '', settingsDir: '',
            timeout: '120', debugMode: true,
            keepInstance: true, updateUserLibs: true
}
