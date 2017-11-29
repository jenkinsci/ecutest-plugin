node('windows') {
    step([$class: 'StopTSBuilder', toolName: 'ECU-TEST', timeout: '120'])
}