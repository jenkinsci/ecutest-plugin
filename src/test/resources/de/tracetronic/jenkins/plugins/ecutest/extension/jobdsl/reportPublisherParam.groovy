def strJobName = 'reportPublisherParam'
def strETInstall = 'ECU-TEST'
def strATXInstall = 'TEST-GUIDE'
def bAllowMissing = true
def bRunOnFailed = true
def bArchiving = false
def bKeepAll = false
def dUnstableThreshold = 15
def dFailedThreshold = 30
def bUnstableOnWarning = true
def bFailedOnError = true
def bUsePersistedSettings = true
def strGeneratorName = 'HTML'
def strCustomGeneratorName = 'Custom'
def strSettingName = 'param'
def strSettingValue = '123'
def strSettingName2 = 'param2'
def strSettingValue2 = '456'
def strCredentialsId = 'credentialsId'
def strTimeout = '600'

freeStyleJob("${strJobName}") {
    publishers {
        publishATX("${strATXInstall}") {
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
            archiving(bArchiving)
            keepAll(bKeepAll)
        }
        publishTRF() {
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
            archiving(bArchiving)
            keepAll(bKeepAll)
        }
        publishUNIT("${strETInstall}") {
            unstableThreshold(dUnstableThreshold)
            failedThreshold(dFailedThreshold)
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
            archiving(bArchiving)
            keepAll(bKeepAll)
        }
        publishETLogs() {
            unstableOnWarning(bUnstableOnWarning)
            failedOnError(bFailedOnError)
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
            archiving(bArchiving)
            keepAll(bKeepAll)
        }
        publishGenerators("${strETInstall}") {
            generators {
                generator("${strGeneratorName}") {
                    settings {
                        setting("${strSettingName}", "${strSettingValue}")
                        setting {
                            name("${strSettingName2}")
                            value("${strSettingValue2}")
                        }
                    }
                    usePersistedSettings(bUsePersistedSettings)
                }
            }
            customGenerators {
                customGenerator("${strCustomGeneratorName}") {
                    settings {
                        setting("${strSettingName}", "${strSettingValue}")
                        setting {
                            name("${strSettingName2}")
                            value("${strSettingValue2}")
                        }
                    }
                }
            }
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
            archiving(bArchiving)
            keepAll(bKeepAll)
        }
        publishTMS("${strETInstall}", "${strCredentialsId}") {
            timeout("${strTimeout}")
            allowMissing(bAllowMissing)
            runOnFailed(bRunOnFailed)
            archiving(bArchiving)
            keepAll(bKeepAll)
        }
    }
}
