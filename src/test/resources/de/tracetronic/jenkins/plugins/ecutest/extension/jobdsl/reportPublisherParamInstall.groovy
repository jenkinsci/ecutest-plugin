def strJobName = 'reportPublisherParamInstall'
def strETInstall = '${ECUTEST}'
def strATXInstall = '${TESTGUIDE}'

freeStyleJob(strJobName) {
    publishers {
        publishATX(strATXInstall)
        publishUNIT(strETInstall)
        publishGenerators(strETInstall)
        publishTMS(strETInstall, "credentialsId")
    }
}
