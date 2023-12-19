/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
def strJobName = 'testBuilderParam'
def strPkgFile = 'test.pkg'
def strPrjFile = 'test.prj'
def strTestFolder = 'test'
def strTbcFile = 'test.tbc'
def strTcfFile = 'test.tcf'
def bForceReload = true
def bLoadOnly = true
def bkeepConfig = true
def strConstantName = 'test'
def strConstantValue = '123'
def strConstantName2 = 'test2'
def strConstantValue2 = '456'
def bRunTest = false
def bRunTraceAnalysis = false
def strParamName = 'param'
def strParamValue = '123'
def strParamName2 = 'param2'
def strParamValue2 = '456'
def strTimeout = '600'
def intTimeout = 600
def bStopOnError = false
def bCheckTestFile = false
def bRecordWarnings = true
def bExecInCurrentPkgDir = false
def strFilterExpression = "Name='TestCase'"
def strJobExecutionMode = 'PARALLEL_EXECUTION'
def intJobExecutionMode = 2
def strScanMode = 'PACKAGES_ONLY'
def bRecursiveScan = true
def strArchivePath = 'test.prz'
def strImportPath = 'import'
def bReplaceFiles = false
def strCredentialsId = 'credentialsId'
def strPackagePath = 'Subject/Test'
def strPackageDirPath = 'Subject/TestDir'
def strProjectPath = 'Root/Test'
def strProjectDirPath = 'Root/TestDir'
def bImportMissingPackages = true
def projectId = '1000'
def bCreateNewPath = true

freeStyleJob("${strJobName}") {
    steps {
        testPackage("${strPkgFile}") {
            testConfig {
                tbcFile("${strTbcFile}")
                tcfFile("${strTcfFile}")
                forceReload(bForceReload)
                loadOnly(bLoadOnly)
                keepConfig(bkeepConfig)
                constants {
                    constant("${strConstantName}", "${strConstantValue}")
                    constant {
                        name("${strConstantName2}")
                        value("${strConstantValue2}")
                    }
                }
            }
            packageConfig {
                runTest(bRunTest)
                runTraceAnalysis(bRunTraceAnalysis)
                parameters {
                    parameter("${strParamName}", "${strParamValue}")
                    parameter {
                        name("${strParamName2}")
                        value("${strParamValue2}")
                    }
                }
            }
            executionConfig {
                timeout("${strTimeout}")
                stopOnError(bStopOnError)
                checkTestFile(bCheckTestFile)
                recordWarnings(bRecordWarnings)
            }
        }
        testProject("${strPrjFile}") {
            testConfig {
                tbcFile("${strTbcFile}")
                tcfFile("${strTcfFile}")
                forceReload(bForceReload)
                loadOnly(bLoadOnly)
                keepConfig(bkeepConfig)
                constants {
                    constant("${strConstantName}", "${strConstantValue}")
                    constant {
                        name("${strConstantName2}")
                        value("${strConstantValue2}")
                    }
                }
            }
            projectConfig {
                execInCurrentPkgDir(bExecInCurrentPkgDir)
                filterExpression("${strFilterExpression}")
                jobExecutionMode(intJobExecutionMode)
            }
            executionConfig {
                timeout(intTimeout)
                stopOnError(bStopOnError)
                checkTestFile(bCheckTestFile)
                recordWarnings(bRecordWarnings)
            }
        }
        testFolder("${strTestFolder}") {
            scanMode("${strScanMode}")
            recursiveScan(bRecursiveScan)
            testConfig {
                tbcFile("${strTbcFile}")
                tcfFile("${strTcfFile}")
                forceReload(bForceReload)
                loadOnly(bLoadOnly)
                keepConfig(bkeepConfig)
                constants {
                    constant("${strConstantName}", "${strParamValue}")
                    constant {
                        name("${strConstantName2}")
                        value("${strParamValue2}")
                    }
                }
            }
            packageConfig {
                runTest(bRunTest)
                runTraceAnalysis(bRunTraceAnalysis)
                parameters {
                    parameter("${strParamName}", "${strConstantValue}")
                    parameter {
                        name("${strParamName2}")
                        value("${strConstantValue2}")
                    }
                }
            }
            projectConfig {
                execInCurrentPkgDir(bExecInCurrentPkgDir)
                filterExpression("${strFilterExpression}")
                jobExecutionMode("${strJobExecutionMode}")
            }
            executionConfig {
                timeout("${strTimeout}")
                stopOnError(bStopOnError)
                checkTestFile(bCheckTestFile)
                recordWarnings(bRecordWarnings)
            }
        }
        importPackages {
            importFromTMS("${strCredentialsId}", "${strPackagePath}", "${strImportPath}", "${strTimeout}")
            importFromTMS("${strCredentialsId}", "${strPackagePath}") {
                importPath("${strImportPath}")
                timeout(intTimeout)
            }
            importFromTMSDir("${strCredentialsId}", "${strPackageDirPath}", "${strImportPath}", "${strTimeout}")
            importFromTMSDir("${strCredentialsId}", "${strPackageDirPath}") {
                importPath("${strImportPath}")
                timeout(intTimeout)
            }
            importAttributesFromTMS("${strCredentialsId}", "${strPkgFile}", "${strTimeout}")
            importAttributesFromTMS("${strCredentialsId}", "${strPkgFile}") {
                timeout(intTimeout)
            }
        }
        importProjects {
            importFromArchive("${strArchivePath}", "${strImportPath}", "${strImportPath}", bReplaceFiles)
            importFromArchive("${strArchivePath}") {
                importPath("${strImportPath}")
                importConfigPath("${strImportPath}")
                replaceFiles(bReplaceFiles)
            }
            importFromTMS("${strCredentialsId}", "${strProjectPath}", "${strImportPath}", bImportMissingPackages,
                "${strTimeout}", "${projectId}")
            importFromTMS("${strCredentialsId}", "${strProjectPath}", {
                importPath("${strImportPath}")
                importMissingPackages(bImportMissingPackages)
                timeout(intTimeout)
            }, "${projectId}")
            importFromTMSDir("${strCredentialsId}", "${strProjectDirPath}", "${strImportPath}", "${strTimeout}")
            importFromTMSDir("${strCredentialsId}", "${strProjectDirPath}") {
                importPath("${strImportPath}")
                timeout(intTimeout)
            }
            importAttributesFromTMS("${strCredentialsId}", "${strPrjFile}", "${strTimeout}")
            importAttributesFromTMS("${strCredentialsId}", "${strPrjFile}") {
                timeout(intTimeout)
            }
        }
        exportPackages {
            exportToTMS("${strCredentialsId}", "${strPkgFile}", "${strPackagePath}", bCreateNewPath, "${strTimeout}")
            exportToTMS("${strCredentialsId}", "${strPkgFile}") {
                exportPath("${strPackagePath}")
                createNewPath(bCreateNewPath)
                timeout(intTimeout)
            }
            exportAttributesToTMS("${strCredentialsId}", "${strPkgFile}", "${strTimeout}")
            exportAttributesToTMS("${strCredentialsId}", "${strPkgFile}") {
                timeout(intTimeout)
            }
        }
        exportProjects {
            exportToTMS("${strCredentialsId}", "${strPrjFile}", "${strProjectPath}", bCreateNewPath, "${strTimeout}")
            exportToTMS("${strCredentialsId}", "${strPrjFile}") {
                exportPath("${strProjectPath}")
                createNewPath(bCreateNewPath)
                timeout(intTimeout)
            }
            exportAttributesToTMS("${strCredentialsId}", "${strPrjFile}", "${strTimeout}")
            exportAttributesToTMS("${strCredentialsId}", "${strPrjFile}") {
                timeout(intTimeout)
            }
        }
    }
}
