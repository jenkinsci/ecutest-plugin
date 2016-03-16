def strJobName = 'reportPublisherParam'
def strETInstall = 'ECU-TEST'
def strATXInstall = 'TEST-GUIDE'
def bAllowMissing = true
def bRunOnFailed = true
def dUnstableThreshold = 15
def dFailedThreshold = 30
def bUnstableOnWarning = true
def bFailedOnError = true
def strGeneratorName = 'HTML'
def strCustomGeneratorName = 'Custom'
def strSettingName = 'param'
def strSettingValue = '123'
def strSettingName2 = 'param2'
def strSettingValue2 = '456'

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
        }
    }
}
