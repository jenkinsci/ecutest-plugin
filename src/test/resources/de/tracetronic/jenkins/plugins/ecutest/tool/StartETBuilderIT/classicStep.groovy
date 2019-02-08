node('windows') {
    step([$class: 'StartETBuilder', toolName: 'ECU-TEST',
          workspaceDir: '', settingsDir: '',
          timeout: '120', debugMode: true,
          keepInstance: true, updateUserLibs: true])
}
