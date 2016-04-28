def strJobName = 'toolBuilderParamInstall'
def strETInstall = '${ECUTEST}'

freeStyleJob(strJobName) {
    steps {
        startET(strETInstall)
        startTS(strETInstall)
        stopET(strETInstall)
        stopTS(strETInstall)
    }
}
