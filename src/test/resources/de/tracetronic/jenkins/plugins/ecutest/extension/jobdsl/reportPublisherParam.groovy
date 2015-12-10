def strJobName = 'reportPublisherParam'
def strETInstall = 'ECU-TEST'
def strATXInstall = 'TEST-GUIDE'
def bAllowMissing = true
def bRunOnFailed = true
def dUnstableThreshold = 15
def dFailedThreshold = 30
def bUnstableOnWarning = true
def bFailedOnError = true

freeStyleJob("${strJobName}") {
    publishers {
        publishATX("${strATXInstall}") {
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
        }
        publishTRF() {
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
        }
        publishUNIT("${strETInstall}") {
            unstableThreshold(dUnstableThreshold)
            failedThreshold(dFailedThreshold)
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
        }
        publishETLogs() {
            unstableOnWarning(bUnstableOnWarning)
            failedOnError(bFailedOnError)
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
        }
    }
}
