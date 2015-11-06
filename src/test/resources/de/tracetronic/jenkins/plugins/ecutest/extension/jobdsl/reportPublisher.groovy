freeStyleJob("reportPublisher") {
    publishers {
        publishATX("TEST-GUIDE") {
            allowMissing(true)
            runOnFailed(true)
        }
        publishTRF() {
            allowMissing(true)
            runOnFailed(true)
        }
        publishUNIT("ECU-TEST") {
            unstableThreshold(15)
            failedThreshold(30)
            allowMissing(true)
            runOnFailed(true)
        }
        publishETLogs() {
            unstableOnWarning(true)
            failedOnError(true)
            allowMissing(true)
            runOnFailed(true)
        }
    }
}
