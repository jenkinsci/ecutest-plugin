node('windows') {
    step([$class: 'StartETBuilder', toolName: 'ECU-TEST',
          workspaceDir: '', settingsDir: 'settings',
          timeout: '120', debugMode: true, keepInstance: true])
}