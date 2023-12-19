freeStyleJob("reportPublisher") {
    publishers {
        publishATX("test.guide") {
            allowMissing(true)
            runOnFailed(true)
            archiving(false)
            keepAll(false)
        }
        publishTRF() {
            allowMissing(true)
            runOnFailed(true)
            archiving(false)
            keepAll(false)
        }
        publishUNIT("ecu.test") {
            unstableThreshold(15)
            failedThreshold(30)
            allowMissing(true)
            runOnFailed(true)
            archiving(false)
            keepAll(false)
        }
        publishETLogs() {
            unstableOnWarning(true)
            failedOnError(true)
            allowMissing(true)
            runOnFailed(true)
            archiving(false)
            keepAll(false)
        }
        publishGenerators("ecu.test") {
            generators {
                generator("HTML") {
                    settings {
                        setting("param", "123")
                        setting {
                            name("param2")
                            value("456")
                        }
                    }
                    usePersistedSettings(true)
                }
            }
            customGenerators {
                customGenerator("Custom") {
                    settings {
                        setting("param", "123")
                        setting {
                            name("param2")
                            value("456")
                        }
                    }
                    usePersistedSettings(false)
                }
            }
            allowMissing(true)
            runOnFailed(true)
            archiving(false)
            keepAll(false)
        }
        publishTMS("ecu.test", "credentialsId") {
            timeout("600")
            allowMissing(true)
            runOnFailed(true)
            archiving(false)
            keepAll(false)
        }
    }
}
