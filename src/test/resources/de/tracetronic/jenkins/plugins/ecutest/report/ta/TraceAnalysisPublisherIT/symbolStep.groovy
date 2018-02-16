node('windows') {
    publishTraceAnalysis toolName: 'ECU-TEST', 
        timeout: '600', mergeReports: true, createReportDirs: true,
        allowMissing: true, runOnFailed: true
}