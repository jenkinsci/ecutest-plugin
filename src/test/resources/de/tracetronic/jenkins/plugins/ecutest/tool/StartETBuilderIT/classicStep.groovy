node('windows') {
    step([$class: 'StartETBuilder', toolName: 'ecu.test',
          workspaceDir: '', settingsDir: '',
          timeout: '120', debugMode: true,
          keepInstance: true, updateUserLibs: true])
}
